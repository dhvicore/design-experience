# Dataset preparation for educational singing

Target: choose a curriculum topic, choose Bollywood/Hollywood/Bhajan/Lo-fi, adapt
educational lyrics to a reviewed score, and synthesize the words on that score.
Data collection is a prerequisite; it is not proof that the current app does this.

## What was actually collected

Four GTSinger English singing segments from two songs, **42.13 seconds total**.
Each has WAV audio, JSON note/word/phone annotations, MusicXML and TextGrid.
Files were downloaded from the publisher's Hugging Face repository. SHA-256,
byte sizes and source URLs are in `manifests/gtsinger_seed_files.json`.
Original recordings, source sung words and derived annotations remain in ignored
`raw/` and `prepared/` folders. They are not embedded in the app or uploaded to GitHub.
This is a tiny ingestion pilot, not a voice-training corpus or a completed model.

```sh
server/.venv/bin/python tools/fetch_singing_seed.py
server/.venv/bin/python tools/prepare_singing_dataset.py
server/.venv/bin/python -m unittest discover -s tools -p test_singing_dataset.py -v
```

The validator checks file hashes, audio duration, note ranges, consistent phone/note
arrays and time boundaries. The two songs have separate train/validation groups;
there is no test set yet. `prepared_seed.jsonl` is a model-neutral manifest, not
TechSinger's binary feature cache. Model-specific vocabulary mapping, feature
extraction and coverage checks still need implementation.

## Source findings

| Source | What it contributes | Current status |
|---|---|---|
| [GTSinger](https://huggingface.co/datasets/GTSinger/GTSinger) | Recorded vocals, realistic scores and phone/word timing | Four English segments collected; no Hindi language |
| [Lakh MIDI](https://colinraffel.com/projects/lmd/) | Candidate symbolic scores for popular songs | Documentation checked; download links unavailable during this run; no MIDI collected |
| [DALI](https://github.com/gabolsgabs/DALI) | Vocal-note/word/line alignment annotations | Documentation checked; audio not assumed bundled or cleared by annotation access |
| [Saraga](https://github.com/MTG/saraga) | Indian art-music pitch, rhythm and phrase annotations | Available source, not downloaded; not an aligned Bollywood Hindi SVS corpus |
| [LAPS-Diff](https://arxiv.org/abs/2507.04966) | Research on Bollywood Hindi singing synthesis | Paper checked; linked dataset short URL unreachable; no usable Hindi checkpoint obtained |
| [TechSinger](https://github.com/gwx314/TechSinger) | Candidate model accepting target phones, pitches, durations and note types | Released checkpoint supports English/Chinese; not installed or tested here |

GTSinger's publisher supplies CC BY-NC-SA 4.0 plus additional terms in
`dataset_license.md`; retain those terms and attribution. Saraga audio/annotations
are CC BY-NC 4.0, while its repository code is AGPL-3.0. Lakh's publisher describes
CC BY 4.0 for the dataset and inconsistent per-file attribution; this catalog does
not establish permission for every underlying composition. `sources.json` records
provenance and unresolved access rather than treating a search result as usable data.

## First educational example

`lessons/rolle.json` defines the non-negotiable theorem facts and category/language
rules. Bollywood uses Hindi; Hollywood uses English; Bhajan uses devotional Hindi
with optional Sanskrit invocation. The current Lo-fi path uses English.

For a selected famous tune we still need a reviewed melody/chorus score, syllable
assignment for our new lyrics, and an appropriate language-capable singing model.
The music score and its aligned phonemes control the vocal; a song title or style
label alone is insufficient. This data work does not make ACE-Step a score-locked
singer and does not prove exact reproduction of a famous melody.

`sample audio + original lyrics/phones + notes/timing -> singer training`

`Rolle facts -> new educational lyrics -> new phones aligned to chosen score -> singing inference -> accompaniment -> app playback`

Keep the chosen melody/phrasing constraints explicit during lyric adaptation.
Use reviewed word/phone durations and slur flags, not naive word chunks. Preserve
phrase connections, score rests and breaths. Before accepting a song, verify every
required theorem condition, all sung lines, recognizable melody and clear vocals.
A pretrained English singing model can be evaluated before considering fine-tuning;
Hindi needs suitable aligned data and a compatible model, neither verified yet.

## Song-reference inventory

`song_reference_inventory.json` lists 56 unique language/title entries from the
existing app catalog. They are **metadata only**, not downloaded famous tunes.
No claim is made that Levitating, Kala Chashma or another requested chorus has been
collected, aligned or approved for training. Selecting one must not silently turn
into an unrelated generated melody.
