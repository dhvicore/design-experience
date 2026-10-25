#!/usr/bin/env python3
"""Validate locally collected GTSinger examples and prepare a model-neutral dataset.

This prepares data, not a trained model or a TechSinger-ready feature cache.
Original sung words and audio remain in ignored raw/prepared folders.
"""
import argparse
import hashlib
import json
import math
import wave
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def validate_words(words, duration):
    if not isinstance(words, list) or not words:
        raise ValueError('Missing word/phone/note annotations.')
    previous = 0.0
    for word in words:
        start, end = float(word['start_time']), float(word['end_time'])
        if not (math.isfinite(start) and math.isfinite(end) and 0 <= start < end <= duration + .05 and start >= previous - .02):
            raise ValueError('Word boundaries are invalid or outside the audio.')
        previous = start
        for names in [('ph', 'ph_start', 'ph_end'), ('note', 'note_start', 'note_end')]:
            arrays = [word[name] for name in names]
            if not arrays[0] or len({len(x) for x in arrays}) != 1:
                raise ValueError('Phone or note arrays have inconsistent lengths.')
            previous_event_start = start
            for left, right in zip(arrays[1], arrays[2]):
                if not (math.isfinite(left) and math.isfinite(right) and start - .02 <= left < right <= end + .02 and left >= previous_event_start - .02):
                    raise ValueError('Phone or note timing is invalid.')
                previous_event_start = left
        if any(not isinstance(pitch, (int, float)) or not math.isfinite(pitch) or not 0 <= pitch <= 127 for pitch in word['note']):
            raise ValueError('Invalid MIDI note value.')
    return words


def prepare():
    source_manifest = json.loads((ROOT / 'datasets/manifests/gtsinger_seed_files.json').read_text())
    records = []
    for item in source_manifest['files']:
        path = ROOT / item['path']
        if hashlib.sha256(path.read_bytes()).hexdigest() != item['sha256']:
            raise ValueError('Collected file checksum changed: ' + item['path'])
        if path.suffix != '.wav':
            continue
        parts = path.relative_to(ROOT / 'datasets/raw/gtsinger').parts
        language, singer, technique, song, group, filename = parts
        with wave.open(str(path)) as audio:
            duration = audio.getnframes() / audio.getframerate()
            rate = audio.getframerate()
            channels = audio.getnchannels()
        words = validate_words(json.loads(path.with_suffix('.json').read_text()), duration)
        sample_id = '#'.join([language, singer, technique, song, group, path.stem])
        identifier = hashlib.sha256(sample_id.encode()).hexdigest()[:20]
        split = 'train' if song == 'let it go' else 'validation'
        prepared_path = ROOT / 'datasets/prepared/gtsinger' / (identifier + '.json')
        prepared_path.parent.mkdir(parents=True, exist_ok=True)
        payload = {'sample_id': sample_id, 'language': 'en', 'singer': singer, 'song': song,
                   'audio_path': str(path.relative_to(ROOT)), 'duration_seconds': duration,
                   'sample_rate': rate, 'channels': channels, 'words': words,
                   'annotation_note': 'Source notes and phone boundaries retained without inventing alignment.'}
        prepared_path.write_text(json.dumps(payload, ensure_ascii=False, indent=2))
        records.append({'id': identifier, 'dataset': 'GTSinger', 'song_group': song,
                        'language': 'en', 'speaker': singer, 'split': split,
                        'audio_path': str(path.relative_to(ROOT)), 'prepared_path': str(prepared_path.relative_to(ROOT)),
                        'duration_seconds': round(duration, 4), 'audio_sha256': item['sha256'],
                        'license': 'CC-BY-NC-SA-4.0 plus publisher terms; see raw license and source registry',
                        'source_url': item['source_url'], 'status': 'validated_ingestion_pilot_not_training_complete'})
    train_songs = {row['song_group'] for row in records if row['split'] == 'train'}
    validation_songs = {row['song_group'] for row in records if row['split'] == 'validation'}
    if train_songs & validation_songs:
        raise ValueError('The same song appears in training and validation.')
    output = ROOT / 'datasets/manifests/prepared_seed.jsonl'
    output.write_text(''.join(json.dumps(row, ensure_ascii=False) + '\n' for row in records))
    report = {'samples': len(records), 'seconds': round(sum(r['duration_seconds'] for r in records), 2),
              'languages': sorted({r['language'] for r in records}), 'song_disjoint_split': True,
              'train_samples': sum(r['split'] == 'train' for r in records),
              'validation_samples': sum(r['split'] == 'validation' for r in records),
              'test_samples': 0, 'model_trained': False,
              'limitations': ['Tiny data-ingestion pilot, not sufficient for singer training.',
                             'No Hindi aligned singing samples have been collected.',
                             'No educational-lyrics-to-famous-melody example is approved yet.',
                             'Model-specific phoneme mapping and feature extraction remain required.']}
    (ROOT / 'datasets/manifests/preparation_report.json').write_text(json.dumps(report, indent=2))
    print(json.dumps(report, indent=2))


if __name__ == '__main__':
    argparse.ArgumentParser(description=__doc__).parse_args()
    prepare()
