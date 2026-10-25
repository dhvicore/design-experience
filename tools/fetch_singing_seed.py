#!/usr/bin/env python3
"""Fetch the four recorded GTSinger pilot examples using the checked file manifest."""
import hashlib
import json
import ssl
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RAW = (ROOT / 'datasets/raw/gtsinger').resolve()
PREFIX = 'https://huggingface.co/datasets/GTSinger/GTSinger/resolve/main/'


def main():
    try:
        import certifi
        context = ssl.create_default_context(cafile=certifi.where())
    except ImportError:
        context = ssl.create_default_context()
    manifest = json.loads((ROOT / 'datasets/manifests/gtsinger_seed_files.json').read_text())
    for row in manifest['files']:
        target = (ROOT / row['path']).resolve()
        if not target.is_relative_to(RAW) or not row['source_url'].startswith(PREFIX):
            raise ValueError('Unexpected source or destination in the dataset manifest.')
        if target.exists() and hashlib.sha256(target.read_bytes()).hexdigest() == row['sha256']:
            print('Verified local:', target.name)
            continue
        if not 0 < row['bytes'] < 8 * 1024 * 1024:
            raise ValueError('Unexpected pilot file size.')
        with urllib.request.urlopen(row['source_url'], context=context, timeout=60) as response:
            content = response.read(row['bytes'] + 1)
        if len(content) != row['bytes'] or hashlib.sha256(content).hexdigest() != row['sha256']:
            raise ValueError('Source file changed; review its manifest before using it: ' + target.name)
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(content)
        print('Downloaded and verified:', target.name)
    print('Pilot collected. Run tools/prepare_singing_dataset.py next. No model has been trained.')


if __name__ == '__main__':
    main()
