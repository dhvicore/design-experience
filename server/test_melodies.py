import hashlib
import re
import unittest
from unittest.mock import patch

import melody_library
from lyric_alignment import fit, repeats_for, ROLLE_RAGHUPATI
from music_server import validate, signature_for


class MelodyTest(unittest.TestCase):
    def test_builtins_have_valid_audio_and_phrase_data(self):
        for entry in melody_library.available():
            profile = melody_library.lookup(entry['id'])
            self.assertEqual(entry['audio_sha256'], hashlib.sha256(profile['path'].read_bytes()).hexdigest())
            self.assertEqual(len(entry['phrases']), len(entry['phrase_syllables']))
            self.assertGreater(entry['duration_seconds'], 30)
        self.assertIsNone(melody_library.lookup('../secret'))

    def test_rolle_demo_preserves_conditions_and_uses_musical_phrases(self):
        profile = melody_library.lookup('raghupati-raghav-raja-ram')
        result = fit('source draft', profile, "Rolle's Theorem", 'Bhajan', 'hi')
        self.assertEqual('curated_demo', result['method'])
        self.assertEqual(6, len(result['lines']))
        self.assertIn('satat', result['lyrics'])
        self.assertIn('avkalan', result['lyrics'])
        self.assertIn('samaan', result['lyrics'])
        self.assertIn('shunya', result['lyrics'])
        self.assertTrue(all(abs(r['target'] - r['estimated']) <= 1 for r in result['lines']))
        self.assertEqual(1, repeats_for(result['lyrics'], profile, 'hi'))

    def test_generic_alignment_keeps_all_words_and_repeats_long_lessons(self):
        profile = melody_library.lookup('raghupati-raghav-raja-ram')
        source = '[Verse]\n' + ' '.join(['function equal sum series limit converges'] * 20)
        result = fit(source, profile)
        words = lambda text: re.sub(r'\[[^\]]+\]', ' ', text).split()
        self.assertEqual(words(source), words(result['lyrics']))
        self.assertGreater(len(result['lines']), len(profile['phrase_syllables']))

    def test_missing_reference_cannot_silently_generate_original_music(self):
        body = dict(topic='Test', song='Test', song_id='not-in-library', lyrics='math song',
                    style_prompt='pop', language='en', bpm=100, melody_mode='reference')
        with self.assertRaisesRegex(ValueError, 'has not been added'):
            validate(body)
        body['melody_mode'] = 'original'
        self.assertEqual('original', validate(body)['melody_mode'])

    def test_cache_changes_when_reference_audio_or_mode_changes(self):
        body = dict(song_id='raghupati-raghav-raja-ram', melody_mode='reference')
        first = signature_for(body)
        with patch('melody_library.lookup', return_value={'audio_sha256': 'different-audio'}):
            self.assertNotEqual(first, signature_for(body))
        self.assertNotEqual(first, signature_for({**body, 'melody_mode': 'original'}))


if __name__ == '__main__':
    unittest.main()
