"""Contract tests for the bridge; no GPU or external generation quota used."""
import io
import json
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

import local_model


class LocalModelTest(unittest.TestCase):
    def request(self):
        return {"style_prompt": "educational pop", "lyrics": "[Verse]\nA correct math lesson",
                "language": "en", "bpm": 100}

    def test_waits_for_completed_job_and_downloads_audio_with_auth(self):
        task = {"data": {"task_id": "job-1"}, "code": 200}
        pending = {"data": [{"status": 0}], "code": 200}
        ready = {"data": [{"status": 1, "result": json.dumps([{"file": "/v1/audio?path=demo.mp3"}])}], "code": 200}
        responses = [io.BytesIO(json.dumps(x).encode()) for x in (task, pending, ready)]
        responses.append(io.BytesIO(b"audio-contract-fixture"))
        with tempfile.TemporaryDirectory() as directory:
            target = Path(directory) / "song.mp3"
            with patch.object(local_model, "settings", return_value=("https://model.example", {"Authorization": "Bearer test-key"})), \
                 patch.object(local_model, "open_request", side_effect=responses) as network, \
                 patch.object(local_model.time, "sleep"):
                mode = local_model.render(self.request(), Path(directory) / "missing.wav", target, lambda _: None)
                self.assertEqual("original_melody", mode)
                self.assertEqual(b"audio-contract-fixture", target.read_bytes())
                self.assertTrue(all(call.args[0].get_header("Authorization") == "Bearer test-key" for call in network.call_args_list))

    def test_provider_failure_does_not_create_audio(self):
        with tempfile.TemporaryDirectory() as directory:
            target = Path(directory) / "song.mp3"
            with patch.object(local_model, "call", side_effect=[{"data": {"task_id": "job"}}, {"data": [{"status": 2, "result": "GPU unavailable"}]}]):
                with self.assertRaisesRegex(RuntimeError, "GPU unavailable"):
                    local_model.render(self.request(), Path(directory) / "missing.wav", target, lambda _: None)
            self.assertFalse(target.exists())

    def test_reference_melody_is_uploaded_for_cover(self):
        with tempfile.TemporaryDirectory() as directory:
            melody = Path(directory) / "melody.wav"
            melody.write_bytes(b"melody-contract-fixture")
            with patch.object(local_model, "settings", return_value=("https://model.example", {})), \
                 patch.object(local_model, "open_request", return_value=io.BytesIO(b'{"data":{"task_id":"cover"},"code":200}')) as network:
                result = local_model.call("/release_task", {"task_type": "cover", "thinking": False}, melody)
                self.assertEqual("cover", result["data"]["task_id"])
                body = network.call_args.args[0].data
                self.assertIn(b'name="src_audio"', body)
                self.assertIn(b"melody-contract-fixture", body)


if __name__ == "__main__":
    unittest.main()
