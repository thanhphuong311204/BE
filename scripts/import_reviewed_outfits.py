"""Import only explicitly reviewed image keys; never overwrite an existing asset."""
import argparse
from contextlib import redirect_stdout
import hashlib
import json
from pathlib import Path
import shutil
from PIL import Image
from style_library import ROOT, connect, save, upload


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--batch', required=True)
    parser.add_argument('--keys', nargs='+', required=True, help='Visually approved slug/outfitKey pairs')
    args = parser.parse_args()
    assert args.batch.isdigit(), 'Batch must be numeric'
    jobs = json.loads((ROOT / 'database/content/outfit-image-jobs.json').read_text(encoding='utf-8'))['jobs']
    by_key = {j['slug'] + '/' + j['outfitKey']: j for j in jobs}
    assert len(set(args.keys)) == len(args.keys)
    entries = []
    for key in args.keys:
        job = by_key[key]
        metadata = ROOT / f"output/outfit-completion/generated-{job['slug']}-{job['outfitKey']}.json"
        meta = json.loads(metadata.read_text(encoding='utf-8'))
        source = Path(meta['path'])
        with Image.open(source) as image:
            assert image.width * 4 == image.height * 3 and max(image.size) <= 4096
            image.verify()
        assert source.stat().st_size <= 5 * 1024 * 1024
        dest = ROOT / f"assets/style-outfits/{job['slug']}-{job['outfitKey']}.png"
        if dest.exists():
            assert hashlib.sha256(dest.read_bytes()).digest() == hashlib.sha256(source.read_bytes()).digest(), 'Existing asset preserved; choose a versioned filename'
        else:
            shutil.copy2(source, dest)
        entries.append({'slug': job['slug'], 'kind': 'outfit', 'outfitKey': job['outfitKey'],
            'file': str(dest.relative_to(ROOT)).replace('\\', '/'), 'caption': job['name'],
            'provenance': 'AI builtin imagegen; adult female; individual visual review approved. Prompt: database/content/outfit-image-jobs.json ' + key})
    manifest = ROOT / f'output/outfit-completion/batch-{args.batch}.json'
    save(manifest, {'images': entries})
    with connect() as db, (ROOT / f'output/outfit-completion/batch-{args.batch}-upload.log').open('w', encoding='utf-8') as log:
        with redirect_stdout(log):
            upload(db, manifest, True)
    shutil.copy2(ROOT / 'output/style-library/upload-results.json', ROOT / f'output/outfit-completion/batch-{args.batch}-results.json')
    for key in args.keys:
        job = by_key[key]
        metadata = ROOT / f"output/outfit-completion/generated-{job['slug']}-{job['outfitKey']}.json"
        meta = json.loads(metadata.read_text(encoding='utf-8'))
        meta['status'] = 'visually-reviewed-and-uploaded'
        save(metadata, meta)
    print(json.dumps({'batch': args.batch, 'reviewedUploaded': len(entries)}))


if __name__ == '__main__':
    main()