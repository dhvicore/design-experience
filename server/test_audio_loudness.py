import math
import subprocess

import imageio_ffmpeg
import struct
import tempfile
import unittest
import wave
from pathlib import Path

from audio_loudness import measure, normalize, ensure_normalized


class LoudnessTest(unittest.TestCase):
    def recording(self, path, silent=False):
        # Quiet, 3-second PCM fixture. No model or network needed.
        with wave.open(str(path), 'wb') as audio:
            audio.setnchannels(1); audio.setsampwidth(2); audio.setframerate(16000)
            audio.writeframes(b''.join(struct.pack('<h', 0 if silent else int(90 * math.sin(2 * math.pi * 440 * t / 16000))) for t in range(48000)))

    def test_quiet_recording_gets_louder_without_clipping_or_duration_change(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'song.mp3'
            self.recording(path)
            report = normalize(path)
            self.assertGreater(report['after_lufs'] - report['before_lufs'], 20)
            self.assertAlmostEqual(-16, report['after_lufs'], delta=1)
            self.assertLess(report['true_peak_db'], -0.1)
            decoded = Path(directory) / 'decoded.wav'
            subprocess.run([imageio_ffmpeg.get_ffmpeg_exe(), '-nostdin', '-loglevel', 'error', '-i', str(path),
                            '-c:a', 'pcm_s16le', str(decoded)], check=True)
            with wave.open(str(decoded)) as audio:
                self.assertAlmostEqual(3, audio.getnframes() / audio.getframerate(), delta=0.1)
            # A second preparation uses the recorded version instead of re-encoding.
            ensure_normalized(path)
            before = path.read_bytes()
            ensure_normalized(path)
            self.assertEqual(before, path.read_bytes())

    def test_silence_is_reported_and_original_file_preserved(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'silent.mp3'
            self.recording(path, silent=True)
            original = path.read_bytes()
            with self.assertRaisesRegex(ValueError, 'silent'):
                normalize(path)
            self.assertEqual(original, path.read_bytes())


if __name__ == '__main__':
    unittest.main()
