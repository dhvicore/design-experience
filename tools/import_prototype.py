"""Validate and refresh Android assets from the sibling original prototype."""
import json
import shutil
from collections import Counter
from pathlib import Path

project = Path(__file__).resolve().parents[1]
source = project.parent / "prototype"
topics = json.loads((source / "studio/topics.json").read_text())
calculus = [t for t in topics if t["unit"].startswith("Unit ")]
assert len(calculus) == 38, "Expected the complete 38-topic Calculus dataset"
assert len({(t["unit"], t["topic"]) for t in calculus}) == 38, "Duplicate topics"
assert Counter(t["unit"] for t in calculus) == {
    "Unit 1": 9, "Unit 2": 5, "Unit 3": 5, "Unit 4": 8, "Unit 5": 11,
}, "Unexpected unit coverage"
for topic in calculus:
    for field in ("topic", "unit", "concept", "formula", "lyrics_en"):
        assert topic.get(field), f"Missing {field}: {topic.get('topic')}"
catalog = json.loads((source / "studio/catalog.json").read_text())
for language, genres in catalog["genres"].items():
    assert genres, f"No genres for {language}"
    for genre in genres.values():
        assert all(genre.get(key) for key in ("name", "instruments", "suno", "songs"))
        for song in genre["songs"]:
            assert all(key in song for key in ("title", "artist", "bpm", "mood"))

assets = project / "app/src/main/assets"
assets.mkdir(parents=True, exist_ok=True)
shutil.copy2(source / "studio/topics.json", assets / "topics.json")
shutil.copy2(source / "studio/catalog.json", assets / "song_catalog.json")
print("Imported and validated 38 Calculus topics and song catalog. No sample audio imported.")
