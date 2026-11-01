"""Probe existing Gemini configuration with synthetic public prompts only.
No source extraction, retries, configuration changes, or database writes.
"""
import json
from pathlib import Path
import time
import requests
from style_library import settings

cfg = settings()
model = cfg.get('GEMINI_MODEL', 'gemini-3.8-flash')
url = 'https://generativelanguage.googleapis.com/v1beta/models/' + model
headers = {'x-goog-api-key': cfg.get('GEMINI_API_KEY', '')}
results = {'model': model, 'automaticRetries': 0, 'cases': []}
cases = [('metadata', None),
         ('minimal', 'Say hello briefly.'),
         ('fashion', 'Suggest a simple school outfit in Vietnamese.'),
         ('palette', 'Suggest a pastel color palette with HEX codes. No verified photos are supplied. Do not invent image URLs.')]
for label, prompt in cases:
    start = time.perf_counter()
    row = {'case': label}
    try:
        payload = None
        if prompt is not None:
            generation = {'maxOutputTokens': 2048, 'temperature': 0.7, 'responseMimeType': 'application/json'}
            if model == 'gemini-3.8-flash': generation['thinkingConfig'] = {'thinkingLevel': 'low'}
            payload = {'contents': [{'role': 'user', 'parts': [{'text': prompt + ' Return JSON with text and blocks fields.'}]}], 'generationConfig': generation}
        r = requests.request('GET' if prompt is None else 'POST', url if prompt is None else url + ':generateContent', headers=headers, json=payload, timeout=(5, 15))
        body = r.json()
        row.update(http=r.status_code, providerStatus=body.get('error', {}).get('status'))
        if prompt is None: row['methods'] = body.get('supportedGenerationMethods')
        else:
            row['finishReasons'] = [c.get('finishReason') for c in body.get('candidates', [])]
            row['usage'] = body.get('usageMetadata')
    except (requests.RequestException, ValueError) as ex:
        row['errorType'] = type(ex).__name__
    row['durationMs'] = round((time.perf_counter() - start) * 1000, 2)
    results['cases'].append(row)
    print(json.dumps(row), flush=True)
    if row.get('http') in (401, 403, 404, 429, 503): break
output = Path(__file__).resolve().parents[1]/'output/nova-end-to-end'
output.mkdir(parents=True, exist_ok=True)
(output/'provider.json').write_text(json.dumps(results, indent=2), encoding='utf-8')
