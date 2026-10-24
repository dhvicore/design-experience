# Syllabus Song Studio — Android

Native Kotlin / Jetpack Compose starter built from the existing Calculus Python
prototype. Open **this folder** in Android Studio, sync Gradle, select `app`, and Run.

## What works in this version

- Explicit demo entry with name and optional unverified SAP ID. This is not authentication.
- Calculus: all **38 topics across five units**, with source concepts and formulas.
- Five language options and the existing language-specific genre/reference-song catalog.
- Music style, reference song, tempo and learning-focus controls.
- Loading and editing the original topic lyrics, plus copying a grounded AI lyric prompt.
- Manual Suno handoff: copy approved lyrics and style, then open Suno.
- Existing English Rolle's Theorem Suno example opens in a browser.
- Offline English/Hindi Rolle's Theorem **computer-voice recordings**, with play,
  pause, seek and replay. These fixed previews do not reflect new preferences or edits.
- Selection and draft state survive ordinary activity recreation.

Branch/year/semester data, real login, live lyric generation, automated synthesis,
song imports and saved library are still team tasks. No credentials or provider API
keys are included. Missing academic mappings are stated rather than invented.

## Reused prototype material

| Original | Android location |
|---|---|
| `../prototype/studio/topics.json` | `app/src/main/assets/topics.json` |
| `../prototype/studio/catalog.json` | `app/src/main/assets/song_catalog.json` |
| `../prototype/demo_songs/Rolle_s_Theorem_EN_twinkle.wav` | `app/src/main/res/raw/rolle_en.wav` |
| Hindi version of the same preview | `app/src/main/res/raw/rolle_hi.wav` |
| Rolle's Suno registry entry | Browser link on the synthesis screen |

The asset file retains the Biology example, but the app explicitly filters the 38
Calculus topics. Original data is preserved. Existing translations and facts need
subject-owner review. The English Rolle's lyric draft adds the missing continuity
and differentiability assumptions without changing the source dataset.

Run `python3 tools/import_prototype.py` from this folder to refresh imported assets
when the prototype content changes. This requires the sibling prototype folder;
it is not needed to build the Android app after cloning the repository.

The Python singing engine depends on macOS `say` and Praat, so it cannot simply run
inside Android. Its existing recordings are reused; natural singing requires a
provider integration or a manual production workflow.

## Files and ownership

Kotlin source root: `app/src/main/java/com/nmims/songstudio/`.

| Person | Starting files / responsibility |
|---|---|
| Dhvani | `ui/Theme.kt`, `ui/SongStudio.kt`, `generation/LyricPrompts.kt`: shared design, customization, draft editor and content-to-lyrics prompts |
| Suraj | `generation/`: lyric provider and singing provider adapters, request state, failures; work with Dhvani on generation UI |
| Aditri | Add `auth/` and `ui/auth/`: real app login/profile, sessions and stored preferences; replace demo entry |
| Krishhma | `data/Curriculum.kt`, assets; add `ui/academic/`: verified branch/year/semester mapping, curriculum import and selection |
| Aman | `ui/PreviewPlayer.kt`; add `library/` and `ui/library/`: playback, audio storage and saved-song records |

Each owner should extract their screens into their feature folder. Coordinate
changes to `ui/SongStudio.kt`, the initial navigation shell, before merging.
The current in-memory demo state should become shared lifecycle-aware state when
real async provider calls are introduced.

**All five** supply their own subject data, keywords/formulas, song selections and
topic-to-style mappings. Each reviews their subject's lyrics/audio and tests their
module's integration. Integration and screenshots are shared responsibilities.

## Provider integration contract

`Topic` supplies curriculum facts. `SongPreferences` supplies language, style,
reference metadata, tempo and learning focus. `LyricPrompts.educational(...)`
builds a backend-ready prompt; `LyricPrompts.music(...)` builds the style prompt.
The stored lyric draft is not an AI-generated response and its contents do not
change with tempo/focus. Those preferences enter the exported prompts.

Suraj should add generation states such as Idle / Loading / Ready / Failed and an
output containing provider job ID, reviewed lyrics, audio location and provenance.
Use a backend for secret keys. Aman consumes the output's audio location and stores
its metadata. Never present prepared audio as a newly generated response.

## Team GitHub workflow

1. Push this Android folder as the repository root. Include `gradlew`, `gradlew.bat`,
   `gradle/wrapper/`, source, assets and documentation.
2. Invite all four teammates. Everyone clones the **same** project.
3. Create feature branches: `feature/ui-lyrics`, `feature/song-synthesis`,
   `feature/login-profile`, `feature/academic-data`, `feature/player-library`.
4. Pull the latest `main`, make a small change, build it, commit, push and open a PR.
5. A teammate reviews; merge only compiling changes. Update feature branches from
   `main` regularly. Do not overwrite each other's shared navigation work.

Shared repository: https://github.com/dhvicore/design-experience

`.gitignore` excludes machine paths, caches, IDE metadata and builds.
Do not upload the Python account database or API keys.

## Build

Use Android Studio's bundled JDK 17+ (the installed Studio JDK works), SDK 36 and
the included Gradle wrapper. Run `./gradlew :app:assembleDebug :app:lintDebug`.
The first build needs internet for dependencies. An ignored `local.properties`
holds the local SDK path; Android Studio creates its own on teammates' machines.

## Manual acceptance checks

1. Enter a demo name; open all five units and verify the topic counts 9 / 5 / 5 / 8 / 11.
2. Change language/style; confirm valid reference-song choices reset correctly.
3. Load a draft, edit it, navigate back and forward, and rotate the screen.
4. Copy the lyric prompt; check topic, focus, language and genre match selections.
5. Open Suno with the copied style and edited lyrics; identify this as manual.
6. For Rolle's Theorem in English/Hindi, play/pause/seek/replay the fixed preview.
7. Background and reopen the app; confirm the preview releases and can play again.
8. Choose another topic or language: it must not play the unrelated Rolle recording.
9. Test no-browser and no-network behavior for the external Suno step.

## References

- Compose: https://developer.android.com/develop/ui/compose/documentation
- Player lifecycle: https://developer.android.com/media/platform/mediaplayer/state-resources
- Provider access: https://platform.suno.com/
- Provider download allowances: https://help.suno.com/en/articles/13614785

