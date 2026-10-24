"""Two-pass loudness normalization for generated MP3s, without changing tempo."""
import json
import math
import re
import subprocess
from pathlib import Path

import imageio_ffmpeg

VERSION = 1


def measure(path):
    command = [imageio_ffmpeg.get_ffmpeg_exe(), '-nostdin', '-hide_banner', '-i', str(path),
               '-af', 'loudnorm=I=-16:TP=-1.5:LRA=11:print_format=json', '-f', 'null', '-']
    result = subprocess.run(command, capture_output=True, text=True, timeout=120, check=True)
    matches = re.findall(r'\{\s*"input_i".*?\}', result.stderr, re.S)
    if not matches:
        raise RuntimeError('Could not measure the generated recording.')
    return json.loads(matches[-1])


def normalize(path):
    path = Path(path)
    before = measure(path)
    if not math.isfinite(float(before['input_i'])):
        raise ValueError('The recording is silent; volume adjustment cannot recover missing vocals.')
    settings = ':'.join([
        'loudnorm=I=-16:TP=-1.5:LRA=11',
        'measured_I=' + before['input_i'], 'measured_TP=' + before['input_tp'],
        'measured_LRA=' + before['input_lra'], 'measured_thresh=' + before['input_thresh'],
        'offset=' + before['target_offset'], 'linear=true',
    ])
    temporary = path.with_name(path.stem + '.loudness.tmp.mp3')
    try:
        subprocess.run([imageio_ffmpeg.get_ffmpeg_exe(), '-nostdin', '-hide_banner', '-loglevel', 'error',
                        '-y', '-i', str(path), '-af', settings, '-ar', '48000', '-c:a', 'libmp3lame',
                        '-b:a', '192k', str(temporary)], capture_output=True, timeout=120, check=True)
        after = measure(temporary)
        if float(after['input_tp']) > -0.1:
            raise RuntimeError('The normalized recording exceeded the peak limit.')
        temporary.replace(path)
        return {'version': VERSION, 'before_lufs': float(before['input_i']),
                'after_lufs': float(after['input_i']), 'true_peak_db': float(after['input_tp'])}
    finally:
        temporary.unlink(missing_ok=True)


def ensure_normalized(path):
    path = Path(path)
    metadata_path = path.with_suffix('.json')
    metadata = json.loads(metadata_path.read_text()) if metadata_path.exists() else {}
    if metadata.get('loudness', {}).get('version') != VERSION:
        metadata['loudness'] = normalize(path)
        metadata_path.write_text(json.dumps(metadata, ensure_ascii=False))
    return metadata['loudness']
