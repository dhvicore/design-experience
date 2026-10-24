"""Adapter for the official locally hosted ACE-Step REST API."""
import json
import os
import shutil
import time
import urllib.parse
import urllib.request
import ssl
from uuid import uuid4
from pathlib import Path
import wave

def settings():
    values = {}
    path = Path(__file__).with_name(".env")
    if path.exists():
        for line in path.read_text().splitlines():
            key, separator, value = line.partition("=")
            if separator and key.strip() in ("LOCAL_MUSIC_URL", "LOCAL_MUSIC_API_KEY"):
                values[key.strip()] = value.strip().strip('"').strip("'")
    base = os.getenv("LOCAL_MUSIC_URL", values.get("LOCAL_MUSIC_URL", "http://127.0.0.1:8001")).rstrip("/")
    token = os.getenv("LOCAL_MUSIC_API_KEY", values.get("LOCAL_MUSIC_API_KEY", ""))
    parsed = urllib.parse.urlparse(base)
    if parsed.scheme != "https" and not (parsed.scheme == "http" and parsed.hostname in ("127.0.0.1", "localhost")):
        raise ValueError("Music service must use HTTPS or local loopback.")
    return base, {"Authorization": "Bearer " + token} if token else {}


def open_request(request, timeout=30):
    try:
        import certifi
        context = ssl.create_default_context(cafile=certifi.where())
    except ImportError:
        context = ssl.create_default_context()
    return urllib.request.urlopen(request, timeout=timeout, context=context)


def call(path, data, melody=None):
    base, headers = settings()
    if melody is None:
        payload = json.dumps(data).encode()
        headers["Content-Type"] = "application/json"
    else:
        boundary = "MusicBoundary" + uuid4().hex
        fields = []
        for key, value in data.items():
            value = json.dumps(value) if isinstance(value, (bool, int, float)) else str(value)
            fields.append(f'--{boundary}\r\nContent-Disposition: form-data; name="{key}"\r\n\r\n{value}\r\n'.encode())
        fields.append(f'--{boundary}\r\nContent-Disposition: form-data; name="src_audio"; filename="melody.wav"\r\nContent-Type: audio/wav\r\n\r\n'.encode())
        fields.extend([melody.read_bytes(), f'\r\n--{boundary}--\r\n'.encode()])
        payload = b"".join(fields)
        headers["Content-Type"] = "multipart/form-data; boundary=" + boundary
    request = urllib.request.Request(base + path, data=payload, headers=headers)
    with open_request(request) as response:
        result = json.load(response)
    if result.get("code", 200) != 200:
        raise RuntimeError(str(result.get("error", result)))
    return result


def render(request, melody: Path, destination: Path, status):
    cover = melody.is_file()
    body = {
        "prompt": request["style_prompt"], "lyrics": request["lyrics"],
        "vocal_language": request["language"], "bpm": request["bpm"],
        "audio_duration": min(90, max(30, int(len(request["lyrics"].split()) * 0.55) + 10)),
        "batch_size": 1, "inference_steps": 8,
        "thinking": False, "use_cot_caption": False, "use_cot_language": False,
        "key_scale": "C Major", "time_signature": "4", "audio_format": "mp3",
        "task_type": "cover" if cover else "text2music",
    }
    if cover:
        with wave.open(str(melody)) as audio:
            body["audio_duration"] = audio.getnframes() / audio.getframerate()
        body["audio_cover_strength"] = 1.0
        body.pop("key_scale", None)
    task_id = call("/release_task", body, melody if cover else None)["data"]["task_id"]
    for _ in range(360):
        rows = call("/query_result", {"task_id_list": [task_id]})["data"]
        if rows:
            row = rows[0]
            if row["status"] == 2:
                raise RuntimeError("The local model could not create this song: " + str(row.get("result", ""))[:250])
            if row["status"] == 1:
                output = json.loads(row["result"])[0]
                audio_path = output["file"]
                parsed = urllib.parse.urlparse(audio_path)
                if parsed.path != "/v1/audio":
                    raise RuntimeError("Unexpected audio endpoint returned by model.")
                base, headers = settings()
                url = base + parsed.path + ("?" + parsed.query if parsed.query else "")
                with open_request(urllib.request.Request(url, headers=headers), timeout=60) as response, destination.open("wb") as target:
                    shutil.copyfileobj(response, target)
                return "reference_cover" if cover else "original_melody"
        status("Creating vocals and music on the connected GPU…")
        time.sleep(2)
    raise TimeoutError("The local model did not finish within 12 minutes.")
