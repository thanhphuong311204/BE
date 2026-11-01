"""Audit, conservatively seed, upload and verify the NOVA editorial library.

Secrets are read locally and never printed. Run from BE; see docs/style-library.md.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import sys
import time
from urllib.parse import urlparse

import pymysql
import requests
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
HTTP = requests.Session()
HTTP.mount('https://', HTTPAdapter(max_retries=Retry(total=4, backoff_factor=1,
    allowed_methods={'GET','POST'}, status_forcelist=[429,502,503,504])))


def settings():
    values = {}
    for line in (ROOT / '.env').read_text(encoding='utf-8-sig').splitlines():
        if '=' in line and not line.lstrip().startswith('#'):
            key, value = line.split('=', 1)
            values[key.strip()] = value.strip().strip('"').strip("'")
    values.update(os.environ)
    return values


def connect():
    cfg = settings()
    url = urlparse(cfg['DB_URL'].removeprefix('jdbc:'))
    return pymysql.connect(host=url.hostname, port=url.port or 3306,
        user=cfg['DB_USERNAME'], password=cfg['DB_PASSWORD'],
        database=url.path.lstrip('/'), charset='utf8mb4',
        cursorclass=pymysql.cursors.DictCursor, autocommit=False,
        connect_timeout=10, read_timeout=30, write_timeout=30)


def rows(db):
    with db.cursor() as cur:
        cur.execute("""SELECT s.id,s.code,s.slug,s.image_url,g.code AS group_code,
            t.name AS name_vi, sg.content,sg.content_version
            FROM styles s JOIN style_groups g ON s.group_id=g.id
            JOIN languages l ON l.code='vi'
            LEFT JOIN style_translations t ON t.style_id=s.id AND t.language_id=l.id
            LEFT JOIN style_guides sg ON sg.style_id=s.id AND sg.language_id=l.id
            WHERE s.is_explore_enabled=TRUE AND s.is_active=TRUE
            ORDER BY g.sort_order,s.sort_order,s.id""")
        return cur.fetchall()


def complete(guide):
    required = ['overview','recognition','silhouette','palette','materials','garments',
        'accessories','occasions','suitability','mistakes','gettingStarted']
    return bool(guide) and all(guide.get(k) for k in required) and len(guide.get('outfits', [])) >= 5


def save(path, value):
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')


def audit(db, output):
    catalog = rows(db)
    for row in catalog:
        row['guide'] = json.loads(row.pop('content')) if row['content'] else None
        row['complete'] = complete(row['guide'])
    result = {'total': len(catalog), 'groups': len({r['group_code'] for r in catalog}),
        'completeGuides': sum(r['complete'] for r in catalog),
        'coversWithUrl': sum(bool(r['image_url']) for r in catalog),
        'missingCovers': [r['slug'] for r in catalog if not r['image_url']],
        'missingGuides': [r['slug'] for r in catalog if not r['complete']], 'styles': catalog}
    save(output, result)
    print(json.dumps({k:v for k,v in result.items() if k != 'styles'}, ensure_ascii=False))


def seed(db, path, apply):
    payload = json.loads(Path(path).read_text(encoding='utf-8'))
    entries = payload['styles']
    assert len(entries) == 40 and len({e['slug'] for e in entries}) == 40
    assert all(complete(e['guide']) for e in entries), 'Incomplete guide payload'
    catalog = rows(db)
    assert {r['slug'] for r in catalog} == {e['slug'] for e in entries}, 'Catalog mismatch'
    backup = ROOT/'output/style-library'/f'before-guides-{time.time_ns()}.json'
    save(backup, catalog)
    inserted, supplemented, preserved = [], [], []
    with db.cursor() as cur:
        cur.execute("SELECT id FROM languages WHERE code='vi'")
        language = cur.fetchone()['id']
        by_slug = {r['slug']:r for r in catalog}
        for entry in entries:
            row = by_slug[entry['slug']]
            cur.execute('SELECT content FROM style_guides WHERE style_id=%s AND language_id=%s FOR UPDATE', (row['id'],language))
            existing = cur.fetchone()
            if existing:
                guide = json.loads(existing['content'])
                # Preserve every existing section including deliberate empty sections.
                additions = {k:v for k,v in entry['guide'].items() if k not in guide}
                if additions:
                    supplemented.append(entry['slug'])
                    guide.update(additions)
                    if apply:
                        cur.execute('UPDATE style_guides SET content=%s,content_version=content_version+1 WHERE style_id=%s AND language_id=%s',
                            (json.dumps(guide, ensure_ascii=False),row['id'],language))
                else:
                    preserved.append(entry['slug'])
            else:
                inserted.append(entry['slug'])
                if apply:
                    cur.execute('INSERT INTO style_guides(style_id,language_id,content) VALUES(%s,%s,%s)',
                        (row['id'],language,json.dumps(entry['guide'],ensure_ascii=False)))
    db.commit() if apply else db.rollback()
    print(json.dumps({'apply':apply,'inserted':inserted,'supplementedMissingSections':supplemented,'preserved':preserved,'backup':str(backup)},ensure_ascii=False))


def image_url_valid(url, cloud=None):
    parsed = urlparse(url or '')
    return parsed.scheme == 'https' and parsed.hostname == 'res.cloudinary.com' and not parsed.username \
        and '/image/upload/' in parsed.path and (not cloud or parsed.path.startswith('/'+cloud+'/'))


def upload(db, manifest, apply):
    entries = json.loads(Path(manifest).read_text(encoding='utf-8'))['images']
    cfg = settings()
    for key in ['CLOUDINARY_CLOUD_NAME','CLOUDINARY_API_KEY','CLOUDINARY_API_SECRET']:
        assert cfg.get(key), 'Cloudinary configuration missing'
    catalog = {r['slug']:r for r in rows(db)}
    hashes, covers, validated = set(), set(), []
    for entry in entries:
        assert entry['slug'] in catalog, 'Unknown style'
        assert entry['kind'] in ['cover','outfit'] and entry.get('provenance'), 'Kind and rights/provenance required'
        if entry['kind'] == 'cover':
            assert entry['slug'] not in covers, 'Only one cover per style per batch'
            covers.add(entry['slug'])
        path = Path(entry['file']).resolve()
        assert path.is_file() and path.stat().st_size <= 5*1024*1024, 'File missing or exceeds 5 MB'
        digest = hashlib.sha256(path.read_bytes()).hexdigest()
        assert digest not in hashes, 'Duplicate image within batch'
        hashes.add(digest)
        with Image.open(path) as image:
            image.verify()
        with Image.open(path) as image:
            assert image.format in ['JPEG','PNG','WEBP'] and max(image.size) <= 4096
            if entry['kind'] == 'cover': assert image.width * 4 == image.height * 3, 'Cover must be exactly 3:4'
        if entry['kind'] == 'outfit':
            guide = json.loads(catalog[entry['slug']]['content'])
            assert entry.get('outfitKey') in {o['key'] for o in guide['outfits']}, 'Unknown outfit formula'
        validated.append((entry,path,digest))
    results = []
    for entry,path,digest in validated:
        row = catalog[entry['slug']]
        if entry['kind'] == 'cover' and row['image_url']:
            results.append({'slug':entry['slug'],'status':'preserved-existing-cover'})
            continue
        if entry['kind'] == 'outfit':
            with db.cursor() as cur:
                cur.execute('SELECT id FROM style_outfit_images WHERE style_id=%s AND sha256=%s', (row['id'],digest))
                if cur.fetchone():
                    results.append({'slug':entry['slug'],'status':'preserved-existing-outfit'})
                    continue
        if not apply:
            results.append({'slug':entry['slug'],'kind':entry['kind'],'status':'validated-dry-run'})
            continue
        public_id = f"nova-atelier/styles/{entry['slug']}/{entry['kind']}/{digest}"
        params = {'public_id':public_id,'timestamp':str(int(time.time())),'overwrite':'false'}
        if cfg.get('CLOUDINARY_DYNAMIC_FOLDERS','true').lower() == 'true': params['asset_folder'] = 'nova-atelier/styles'
        signed = '&'.join(f'{k}={v}' for k,v in sorted(params.items()))+cfg['CLOUDINARY_API_SECRET']
        form = {**params,'api_key':cfg['CLOUDINARY_API_KEY'],'signature':hashlib.sha1(signed.encode()).hexdigest()}
        with path.open('rb') as file:
            response = HTTP.post(f"https://api.cloudinary.com/v1_1/{cfg['CLOUDINARY_CLOUD_NAME']}/image/upload",
                data=form,files={'file':(path.name,file)},timeout=(10,60))
        if response.status_code != 200:
            raise RuntimeError(f'Cloudinary rejected upload: HTTP {response.status_code}; response body withheld')
        provider = response.json()
        assert provider.get('public_id') == public_id and image_url_valid(provider.get('secure_url'),cfg['CLOUDINARY_CLOUD_NAME'])
        url = provider['secure_url']
        with db.cursor() as cur:
            if entry['kind'] == 'cover':
                cur.execute('UPDATE styles SET image_url=%s WHERE id=%s AND (image_url IS NULL OR image_url=\'\')', (url,row['id']))
                attached = cur.rowcount == 1
            else:
                cur.execute('INSERT INTO style_outfit_images(style_id,outfit_key,image_url,public_id,caption,sha256,provenance) VALUES(%s,%s,%s,%s,%s,%s,%s)',
                    (row['id'],entry['outfitKey'],url,public_id,entry.get('caption',''),digest,entry['provenance']))
                attached = True
        db.commit()
        # Journal each provider asset immediately so interrupted batches remain recoverable.
        results.append({'slug':entry['slug'],'kind':entry['kind'],'url':url,'publicId':public_id,'sha256':digest,
            'status':'attached' if attached else 'uploaded-not-attached-concurrent-edit-preserved'})
        save(ROOT/'output/style-library/upload-results.json',results)
    print(json.dumps(results,ensure_ascii=False))


def verify(db, output):
    result = []
    for row in rows(db):
        url = row['image_url']
        item = {'slug':row['slug'],'url':url,'ok':False}
        if image_url_valid(url):
            try:
                response = HTTP.get(url,timeout=(10,30))
                item['status'] = response.status_code
                if response.status_code == 200 and response.headers.get('Content-Type','').startswith('image/'):
                    import io
                    with Image.open(io.BytesIO(response.content)) as image:
                        image.verify()
                    item['ok'] = True
            except (requests.RequestException,OSError): item['error'] = 'unreachable-or-invalid-image'
        result.append(item)
    save(output,{'verifiedCovers':sum(i['ok'] for i in result),'styles':result})
    print(json.dumps({'verifiedCovers':sum(i['ok'] for i in result),'missingOrBroken':[i['slug'] for i in result if not i['ok']]},ensure_ascii=False))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('command', choices=['audit','seed-guides','upload','verify'])
    parser.add_argument('--input')
    parser.add_argument('--output',default=str(ROOT/'output/style-library/audit.json'))
    parser.add_argument('--apply',action='store_true',help='Explicitly write to DB/Cloudinary; default dry-run')
    args = parser.parse_args()
    try:
        with connect() as db:
            if args.command == 'audit': audit(db,args.output)
            elif args.command == 'seed-guides': seed(db,args.input,args.apply)
            elif args.command == 'upload': upload(db,args.input,args.apply)
            else: verify(db,args.output)
    except Exception as exc:
        # Provider/DB exception messages can contain connection details or secrets.
        print(f'Operation failed: {type(exc).__name__}; no credentials or provider response logged.',file=sys.stderr)
        return 1
    return 0


if __name__ == '__main__':
    sys.exit(main())
