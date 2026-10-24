# Syllabus Song Studio — Android

Kotlin / Jetpack Compose app based on the original Calculus prototype.
Shared repository: https://github.com/dhvicore/design-experience

Open this folder in Android Studio, sync Gradle, choose `app` and Run.

## Current functionality

- Signup and login with device-local SQLite accounts, normalized email addresses,
  unique-account validation and salted PBKDF2-HMAC-SHA256 password hashes.
- Signup returns to login. Passwords and login tokens are not saved in UI state.
- The session ends when the app backgrounds; reopening requires login. Rotation
  preserves an active session. Logout is available in the header.
- Calculus: all 38 topics across five units (9 / 5 / 5 / 8 / 11), with concepts,
  formulas and existing multilingual lyric drafts from the prototype.
- Independent lyric-language and music-collection choices: Hollywood/English,
  Bollywood/Hindi, Marathi, Gujarati and Sanskrit/devotional.
- Existing genre and song catalog, including pop, rock, hip-hop, romantic, bhajan
  and regional styles; an added lo-fi style uses the collection's reference list.
- Multiple reference songs per style, student learning focus and tempo selection.
- Editable local syllabus-based lyric drafts. This is not a live LLM response.

## Audio generation is not connected

The user currently has a Suno website account, with no configured API connection.
There is no working automatic singing service in this app yet. The synthesis screen
states this; it must not present prepared recordings as newly generated songs.
The old Twinkle-based recordings and external example playback have been removed
at the user's request. There are no prompt-copy or manual-Suno handoff buttons.

Requested exact-song adaptations require a usable authorized melody source and a
singing provider that supports that conditioning. A song title, YouTube listing or
style prompt does not itself create an exact-melody adaptation. Provider access,
capabilities and permitted inputs need verification before connecting synthesis.

Remaining features: real provider-backed lyrics and audio generation, playback of
successful generated output, saved library, cloud accounts if desired, and verified
branch/year/semester mappings. SAP portal authentication is excluded for now.
Device-local accounts do not verify NMIMS affiliation or sync between devices.

## Reused data

- `../prototype/studio/topics.json` → `app/src/main/assets/topics.json`
- `../prototype/studio/catalog.json` → `app/src/main/assets/song_catalog.json`
- Android filters the dataset to the 38 Calculus topics. No other subject syllabus
  is invented. Original Biology example remains in the source asset only.
- `python3 tools/import_prototype.py` refreshes and validates these assets from the
  sibling prototype folder. It does not import any sample audio.
- The English Rolle's draft explicitly adds continuity on the closed interval and
  differentiability inside it. All source translations/content need subject review.

## Team ownership

Kotlin source root: `app/src/main/java/com/nmims/songstudio/`.

| Person | Responsibility / starting files |
|---|---|
| Dhvani | UI/UX, customization and lyrics: `ui/`, `generation/LyricPrompts.kt` |
| Suraj | Real lyric/singing provider integration and request state: `generation/` |
| Aditri | Login/profile/session: `auth/`, authentication part of `MainActivity.kt` |
| Krishhma | Academic selection and curriculum database: `data/`, assets, academic screens |
| Aman | Generated audio player, library/history and storage; add `playback/` and `library/` |

All five supply and validate their own subject data, keywords/formulas, song
selections and topic mappings. Integration, tests, screenshots and documentation
are shared responsibilities. Coordinate changes to the initial `ui/SongStudio.kt`
navigation shell; extract feature screens into separate files as work proceeds.

## GitHub workflow

1. Invite all four teammates. Everyone clones the same repository.
2. Branch from the latest `main`: `feature/ui-lyrics`, `feature/song-synthesis`,
   `feature/login-profile`, `feature/academic-data`, `feature/player-library`.
3. Make small changes, build/test, commit and push your branch, then open a PR.
4. A teammate reviews before merging. Update your branch from `main` regularly.

Do not upload API keys, personal account databases or machine configuration.
`.gitignore` excludes build outputs, IDE metadata, local SDK paths and credentials.

## Build and verification

Use Android Studio's bundled JDK and SDK 36, with the included Gradle wrapper.
The first sync may need internet for dependencies.

```sh
./gradlew :app:assembleDebug :app:lintDebug
./gradlew :app:connectedDebugAndroidTest
```

Emulator tests cover persistent account login, wrong/unknown passwords, duplicate
accounts, invalid signup inputs, logout on backgrounding, session preservation on
rotation, all five Calculus units and multiple music choices. Test accounts use a
separate temporary test database and do not modify the real account database.

## References

- https://developer.android.com/develop/ui/compose/documentation
- https://platform.suno.com/
