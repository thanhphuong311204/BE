"""Read-only outfit inventory and remote verification; no secrets in reports."""
import argparse
from concurrent.futures import ThreadPoolExecutor
import hashlib
import io
import json
from pathlib import Path
from PIL import Image
import requests
from style_library import ROOT, connect, rows, save, image_url_valid, settings


def inventory(catalog, gallery):
    result = []
    for style in catalog:
        guide = json.loads(style['content'])
        for outfit in guide['outfits']:
            candidates = [i for i in gallery if i['slug'] == style['slug'] and i['outfit_key'] == outfit['key']]
            if outfit.get('imageUrl'):
                candidates.append({'image_url': outfit['imageUrl'], 'source': 'guide'})
            result.append({'slug': style['slug'], 'outfitKey': outfit['key'], 'name': outfit['name'],
                'items': outfit['items'], 'candidates': candidates})
    return result


def remote(url):
    record = {'url': url, 'valid': False}
    if not image_url_valid(url, settings()['CLOUDINARY_CLOUD_NAME']):
        record['error'] = 'invalid-cloudinary-url'
        return record
    try:
        response = requests.get(url, timeout=(10, 45))
        record['httpStatus'] = response.status_code
        if response.status_code != 200 or not response.headers.get('Content-Type', '').startswith('image/'):
            return record
        with Image.open(io.BytesIO(response.content)) as image:
            record['width'], record['height'] = image.size
            image.verify()
        record['sha256'] = hashlib.sha256(response.content).hexdigest()
        record['valid'] = record['width'] * 4 == record['height'] * 3
        if not record['valid']:
            record['error'] = 'not-3-by-4'
    except (requests.RequestException, OSError):
        record['error'] = 'unreachable-or-undecodable'
    return record


def audit(verify=False):
    with connect() as db:
        catalog = rows(db)
        with db.cursor() as cur:
            cur.execute('SELECT s.slug,i.outfit_key,i.image_url,i.sha256 FROM style_outfit_images i JOIN styles s ON s.id=i.style_id')
            gallery = cur.fetchall()
    formulas = inventory(catalog, gallery)
    assert len(catalog) == 40 and len(formulas) == 201, 'Catalog/formula count changed'
    baseline_path = ROOT / 'output/outfit-completion/baseline.json'
    if baseline_path.exists():
        before = {s['slug']: s for s in json.loads(baseline_path.read_text(encoding='utf-8'))['styles']}
        for style in catalog:
            assert style['content'] == before[style['slug']]['content'], 'Original guide changed'
            assert style['image_url'] == before[style['slug']]['image_url'], 'Original cover changed'
    urls = sorted({c['image_url'] for f in formulas for c in f['candidates']})
    checked = {}
    if verify:
        with ThreadPoolExecutor(max_workers=6) as executor:
            checked = {r['url']: r for r in executor.map(remote, urls)}
    owners = {}
    for formula in formulas:
        valid = [c['image_url'] for c in formula['candidates'] if (
            checked.get(c['image_url'], {}).get('valid') if verify else image_url_valid(c['image_url']))]
        formula['validUrls'] = list(dict.fromkeys(valid))
        for url in formula['validUrls']:
            owners.setdefault(url, set()).add((formula['slug'], formula['outfitKey']))
    reused = {url for url, keys in owners.items() if len(keys) > 1}
    hashes = {}
    for url, result in checked.items():
        if result.get('valid'):
            hashes.setdefault(result['sha256'], set()).add(url)
    for same in hashes.values():
        if len(same) > 1:
            reused.update(same)
    for formula in formulas:
        formula['validUrls'] = [u for u in formula['validUrls'] if u not in reused]
    good = sum(bool(f['validUrls']) for f in formulas)
    report = {'totalOutfits': len(formulas), 'outfitsWithValidImages': good,
        'missingImages': len(formulas) - good, 'remoteVerified': verify,
        'stylesAndGuidesPreserved': True, 'reusedUrlsRejected': sorted(reused),
        'images': list(checked.values()), 'outfits': formulas}
    save(ROOT / 'output/outfit-completion/inventory.json', report)
    jobs = json.loads((ROOT / 'database/content/outfit-image-jobs.json').read_text(encoding='utf-8'))['jobs']
    missing = {(f['slug'], f['outfitKey']) for f in formulas if not f['validUrls']}
    save(ROOT / 'database/content/missing-outfit-images.json', [j for j in jobs if (j['slug'], j['outfitKey']) in missing])
    print(json.dumps({k: v for k, v in report.items() if k not in ['images', 'outfits', 'reusedUrlsRejected']}))
    return report


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--verify', action='store_true')
    args = parser.parse_args()
    audit(args.verify)