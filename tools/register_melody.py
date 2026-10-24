#!/usr/bin/env python3
"""Developer setup: prepare a supplied chorus for the existing Colab cover pipeline."""
import argparse
import json
import math
import re
import subprocess
import sys
import tempfile
import wave
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / 'server'))
import imageio_ffmpeg
from audio_loudness import measure


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('audio', type=Path, help='Your supplied melody or hummed guide: MP3, M4A or WAV')
    parser.add_argument('--song-id', required=True)
    parser.add_argument('--title', required=True)
    parser.add_argument('--bpm', required=True, type=int)
    parser.add_argument('--start', type=float, default=0)
    parser.add_argument('--seconds', type=float, default=30)
    parser.add_argument('--phrase-syllables', default='', help='Measured phrase syllable counts, comma separated; optional')
    args = parser.parse_args()
    if not re.fullmatch(r'[a-z0-9]+(?:-[a-z0-9]+)*', args.song_id):
        parser.error('Song id must be a lowercase slug such as kala-chashma.')
    if not args.audio.is_file() or not 30 <= args.bpm <= 250 or not 10 <= args.seconds <= 90 or args.start < 0:
        parser.error('Supply an existing audio file, BPM 30–250, start >= 0 and clip length 10–90 seconds.')
    try:
        budgets = [int(x.strip()) for x in args.phrase_syllables.split(',') if x.strip()]
        if any(x < 1 or x > 100 for x in budgets):
            raise ValueError()
    except ValueError:
        parser.error('Phrase syllables must be positive integers up to 100.')
    folder = ROOT / 'server/melodies'
    folder.mkdir(exist_ok=True)
    destination = folder / (args.song_id + '.wav')
    if destination.exists():
        parser.error('This reference already exists. Move it aside before replacing it.')
    with tempfile.TemporaryDirectory() as temporary:
        prepared = Path(temporary) / 'reference.wav'
        subprocess.run([imageio_ffmpeg.get_ffmpeg_exe(), '-nostdin', '-hide_banner', '-loglevel', 'error',
                        '-ss', str(args.start), '-i', str(args.audio.resolve()), '-t', str(args.seconds),
                        '-vn', '-ar', '48000', '-ac', '2', '-c:a', 'pcm_s16le', str(prepared)], check=True)
        with wave.open(str(prepared)) as audio:
            duration = audio.getnframes() / audio.getframerate()
        if not math.isfinite(float(measure(prepared)['input_i'])):
            parser.error('The selected reference is silent. Supply a recording containing the melody.')
        if duration < 10:
            parser.error('The selected section contains less than ten seconds of audio.')
        metadata = {'title': args.title, 'bpm': args.bpm, 'duration_seconds': duration,
                    'phrase_syllables': budgets, 'source': 'Developer-supplied melody reference',
                    'timing_note': 'User-measured phrase lengths.' if budgets else 'Phrase timing not yet supplied.'}
        destination.write_bytes(prepared.read_bytes())
        destination.with_suffix('.json').write_text(json.dumps(metadata, ensure_ascii=False, indent=2))
    print('Prepared:', destination)
    print('Reopen the app → Hindi → select the song → Follow melody → generate/review lyrics → Generate song.')
    if not budgets:
        print('Audio conditioning is ready; phrase fitting still needs measured syllable counts and listening review.')


if __name__ == '__main__':
    main()
