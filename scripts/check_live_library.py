"""Read-only MySQL -> authenticated local API audit; JWT never written or printed."""
import base64
import hashlib
import hmac
import json
from pathlib import Path
import time
import argparse
from style_library import connect, settings, rows, complete, HTTP, save, ROOT

cfg = settings()
parser = argparse.ArgumentParser()
parser.add_argument('--port', type=int, default=8080)
args = parser.parse_args()
with connect() as db:
    catalog = rows(db)
    with db.cursor() as cur:
        cur.execute("SELECT id FROM users WHERE status='ACTIVE' LIMIT 1")
        user = cur.fetchone()['id']
def b64(value):
    return base64.urlsafe_b64encode(value).rstrip(b'=')
header = b64(b'{"alg":"HS256","typ":"JWT"}')
claims = b64(json.dumps({'iss':'fashion-api','sub':str(user),'iat':int(time.time()),'exp':int(time.time())+600}).encode())
signed = header+b'.'+claims
token = (signed+b'.'+b64(hmac.new(cfg['JWT_SECRET'].encode(),signed,hashlib.sha256).digest())).decode()
headers = {'Authorization':'Bearer '+token}
base = f'http://localhost:{args.port}/api/explore/styles'
response = HTTP.get(base,headers=headers,timeout=20)
response.raise_for_status()
listing = response.json()
assert len(listing['styles']) == 40 and len(listing['groups']) == 5
details = []
for row in catalog:
    response = HTTP.get(base+'/'+row['slug'],headers=headers,timeout=20)
    response.raise_for_status()
    detail = response.json()
    assert complete(detail['guide']), row['slug']
    assert detail['style']['imageUrl'] == row['image_url']
    assert detail['guide'] == json.loads(row['content']), row['slug']
    details.append(detail)
save(ROOT/'output/style-library/live-api.json', {'catalog':listing,'details':details})
print(json.dumps({'apiStyles':len(details),'completeGuides':40,'covers':sum(bool(d['style']['imageUrl']) for d in details),'formulas':sum(len(d['guide']['outfits']) for d in details),'galleryImages':sum(len(d['images']) for d in details)}))
