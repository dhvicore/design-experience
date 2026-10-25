import unittest
from prepare_singing_dataset import validate_words


class AnnotationValidationTest(unittest.TestCase):
    def example(self):
        return [{'word': 'lesson', 'start_time': 0.1, 'end_time': 0.8,
                 'ph': ['L', 'EH1', 'S', 'AH0', 'N'],
                 'ph_start': [.1, .2, .3, .4, .6], 'ph_end': [.2, .3, .4, .6, .8],
                 'note': [60], 'note_start': [.1], 'note_end': [.8]}]

    def test_valid_aligned_phrase_is_retained(self):
        data = self.example()
        self.assertEqual(data, validate_words(data, 1.0))

    def test_truncated_audio_is_rejected(self):
        with self.assertRaisesRegex(ValueError, 'outside the audio'):
            validate_words(self.example(), .4)

    def test_inconsistent_phone_arrays_are_rejected(self):
        data = self.example(); data[0]['ph_end'] = [.8]
        with self.assertRaisesRegex(ValueError, 'inconsistent lengths'):
            validate_words(data, 1.0)

    def test_notes_outside_the_phrase_are_rejected(self):
        data = self.example(); data[0]['note_end'] = [1.0]
        with self.assertRaisesRegex(ValueError, 'timing is invalid'):
            validate_words(data, 1.0)


if __name__ == '__main__':
    unittest.main()
