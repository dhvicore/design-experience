# Syllabus Song Studio — Android

Kotlin / Jetpack Compose app with the complete 38-topic Calculus dataset.
Repository: https://github.com/dhvicore/design-experience

## Start the singing engine with Colab

1. Open [the GPU notebook](https://colab.research.google.com/github/dhvicore/design-experience/blob/main/notebooks/Calculus_Singing_GPU.ipynb).
2. Choose **Runtime → Change runtime type → T4 GPU**.
3. Run cells 1 and 2. Dependencies and several GB of weights download on the first run.
4. Run cell 3. It must produce a real sung Calculus clip and an audio player. If it
   errors, resolve that error before claiming the app's generation is working.
5. Run cell 4. It starts a temporary API-key-protected service and downloads
   `song-service.env`. Move that file into `server/` and rename it **`.env`**.
   Do not share its contents or commit it. The existing `.gitignore` excludes it.
6. On the Mac, double-click `server/Start Music Server.command`; keep its terminal
   and the Colab runtime open. The bridge binds only to the Mac's loopback address.
7. Run the Android app on the emulator. Log in, select a topic, choose music,
   create/review lyrics and press **Generate song**. After completion, press Play.

The bridge reads `.env` on requests, so a new Colab endpoint can be configured
without rebuilding the APK. The endpoint changes when the temporary tunnel is
restarted. If Colab stops, new generation stops; completed app audio is kept offline.

This is an emulator demo: Android talks to the Mac at `10.0.2.2:8787`, and the Mac
calls the GPU API over HTTPS. No student account details are sent to the GPU;
only curriculum lyrics and musical parameters are submitted. Physical-device
networking and a production-hosted backend are not configured.

## What is implemented

- Persistent device-local signup/login with salted PBKDF2 password hashes, email
  validation and duplicate-account checks. Backgrounding ends the session; rotation
  preserves it. There is no SAP/portal authentication or cross-device account sync.
- **Calculus Units 1–5: 38 topics**, distributed 9 / 5 / 5 / 8 / 11, using the source
  explanations, formulas and multilingual lyric content from the Python prototype.
- Exactly four initial music choices: **Hindi, English, Bhajan, Lo-fi**.
- Four recommendations vary by topic; **Browse all songs** expands the category's
  catalog. Bollywood includes Chikni Chameli and Chammak Challo.
- Lyric drafts change language/form, phrase grouping and refrain with category and
  song selection. They are locally assembled from syllabus lyrics, not a live LLM.
  Bhajan uses devotional Hindi with a Sanskrit invocation. Subject owners still
  need to review source translations and mathematical assumptions.
- The app submits real generation jobs, polls progress, downloads completed audio,
  and plays it with pause/seek/replay. Failure is shown as failure, not fake success.
- The old import screen, prompt-copy controls, Twinkle recordings and example-song
  playback have been removed.

## Important generation distinction

**Exact famous-song melodies are not supplied by their titles.** Without melody
reference data, the model creates an **original melody in the requested style**;
that mode is explicitly labelled in the app. Changing phrase grouping does not
prove an exact tune fit. The supplied-reference cover path also needs listening
and alignment checks before it can be described as faithful to a selected tune.

To add a melody dataset, place an authorized reference WAV in `server/melodies/`
using the song ID filename, e.g. `chikni-chameli.wav`. This is developer setup,
not a student import step. The adapter uploads it as source audio for cover mode.
MIDI/hummed references must first be rendered/prepared as an appropriate WAV.
Do not upload arbitrary YouTube recordings or train on music without the needed rights.

The model is ACE-Step 1.5, run from its official source. This project uses pretrained
inference; it does not train a new singing model. The public hosted demos failed
with GPU/quota errors during our checks; the Colab notebook is the selected route.
The notebook was syntax-checked locally. Live Colab model startup, generation quality
and the final app-to-Colab audio round trip require the user's active GPU runtime.

## Source and ownership

Kotlin source root: `app/src/main/java/com/nmims/songstudio/`.

| Person | Responsibility / files |
|---|---|
| Dhvani | UI/UX and customization: `ui/`, `data/MusicChoices.kt`; lyric drafting: `generation/LyricPrompts.kt` |
| Suraj | Singing integration: `generation/SingingClient.kt`, `server/`, Colab notebook; improve real lyric generation/alignment with Dhvani |
| Aditri | Signup/login/profile/session: `auth/`, `MainActivity.kt` |
| Krishhma | Academic selections and curriculum database: `data/Curriculum.kt`, assets, academic UI |
| Aman | Playback and audio persistence: `playback/`; extend with saved-library/history browsing |

All five supply and validate their own subject datasets, keywords/formulas, song
selections and topic mappings. Integration, screenshots and testing are shared.
Coordinate edits to `ui/SongStudio.kt` and extract module screens as work expands.

## Reusing the existing prototype

`tools/import_prototype.py` validates and refreshes `topics.json` and
`song_catalog.json` from the sibling original `prototype/` folder. It imports no
sample audio. Only Calculus is exposed; other subjects await the team's real data.
Branch/year/semester mappings must likewise come from the verified curriculum.

## Build and tests

Open this repository folder in Android Studio and sync. SDK 36 and Android Studio's
JDK work with the included Gradle wrapper.

```sh
./gradlew :app:assembleDebug :app:lintDebug
./gradlew :app:connectedDebugAndroidTest
cd server
python3 -m unittest test_local_model -v
```

Emulator tests cover persistent accounts, invalid login/signup, background logout,
rotation, all Calculus units, music recommendations, lyric variations and native
playback. Audio playback tests use a generated test signal, not a produced song.
Python adapter tests mock the GPU service to check authentication headers, job
polling, error handling and reference uploads. These are not proof of live model
output or exact melody fidelity; the Colab singing test supplies that evidence.

## GitHub workflow

Clone the same repository, branch from updated `main`, build/test a small change,
then push your feature branch and open a PR for teammate review. Branches:
`feature/ui-lyrics`, `feature/song-synthesis`, `feature/login-profile`,
`feature/academic-data`, `feature/player-library`.

Keep API keys, local account data, generated audio, model weights and SDK paths out
of GitHub. Generated files, `.env`, virtual environments and build outputs are ignored.

## References

- https://github.com/ace-step/ACE-Step-1.5
- https://github.com/ace-step/ACE-Step-1.5/blob/main/docs/en/API.md
- https://github.com/ace-step/ACE-Step-1.5/blob/main/docs/en/INSTALL.md
- https://developers.cloudflare.com/tunnel/get-started/quick-tunnels/
- https://developer.android.com/develop/ui/compose/documentation
