"""Reference melody metadata and audio available to the developer-managed library."""
import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent


def lookup(song_id):
    if not isinstance(song_id, str) or not song_id.replace("-", "").isalnum():
        return None
    custom = ROOT / "melodies"
    if (custom / (song_id + ".wav")).is_file():
        path = custom / (song_id + ".wav")
        manifest = custom / (song_id + ".json")
        metadata = json.loads(manifest.read_text()) if manifest.exists() else {}
        return {**metadata, "id": song_id, "path": path,
                "audio_sha256": hashlib.sha256(path.read_bytes()).hexdigest()}
    catalog = json.loads((ROOT / "builtin_melodies/catalog.json").read_text())
    for metadata in catalog:
        if metadata["id"] == song_id:
            return {**metadata, "path": ROOT / "builtin_melodies" / (song_id + ".wav")}
    return None


def available():
    ids = {m["id"] for m in json.loads((ROOT / "builtin_melodies/catalog.json").read_text())}
    custom = ROOT / "melodies"
    if custom.exists():
        ids.update(path.stem for path in custom.glob("*.wav"))
    return [{key: value for key, value in lookup(song_id).items() if key != "path"}
            for song_id in sorted(ids) if lookup(song_id)]
