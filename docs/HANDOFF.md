# Sliding Numbers: City Puzzle 3.0: handoff

Name (chosen by DP, replaces "Sliding Numbers+"): Play Store title **Sliding Numbers: City Puzzle** (set in Play Console), launcher label **Sliding Numbers**. Suggested short description: "Slide the tiles, solve number puzzles and tour the world's cities". The package name is unchanged.

## Who and what
- Owner: DP, studio **Pixel Pocket** (Instagram @pixelpocket.apps).
- Native Kotlin/Compose rebuild of **Sliding Numbers+**, now Sliding Numbers: City Puzzle (`com.dpjha.slidingnumbersplus`), versionCode 304, upgrading the existing Play listing. It follows his ColorSort game's systems and polish, with sliding-puzzle gameplay.
- Work is on branch `v3-rewrite`. `main` holds an unrelated old React project.
- There is no Android SDK in the cloud workspace: **CI is the only compiler**. Push, then read the result.

## Locked design
DP approved the web preview and asked for the full build. The reference is `docs/design/locked/preview.html` (`build.py` embeds the instrument samples). Keep these:
- Living city backdrop: city × sunrise/day/sunset/night × clear/rain/snow/autumn/spring. Each city's five levels walk through different skies (`Levels.moods`).
- The board carries the same city, season and time: translucent tiles, themed rim, weather inside the board.
- Sliding must feel instant: 45 ms glide, swipe commits at 20% of a cell.
- Sound stays soft (he asked twice to lower it). Recorded instruments: tonejs-instruments, CC BY 3.0, so keep the credit line.
- Boards, rows × cols: 3×3, 4×3, 4×4, 5×4, 5×5, 6×5, 6×6.

## What is built
| Area | Where |
|---|---|
| Engine, levels, par, moods, daily puzzle | `game/PuzzleEngine.kt`, `game/Levels.kt`, `game/BoardTable.kt` (pre-solved boards: par = about 2.5 x best moves + 2) |
| Moves, undo, free moves, hints | `game/Session.kt`, `game/Solver.kt` |
| Save format, economy, streak, chests, achievements, cloud merge | `game/Player.kt`, `game/Rules.kt` |
| Saving and upgrade from the 2.x and MAUI builds | `data/GameStore.kt` |
| Scenes, tiles, board, sound | `ui/art/`, `ui/board/`, `audio/` |
| Home, levels, game and win, rewards, themes, shop, passport, stats | `ui/screens/` |
| Play Games, ads with consent, review prompt | `services/` |

`ui/art/Landmarks.kt` serves cities 8 to 21 and is a **simple first draft**. They also share three music tracks with the first seven. The daily puzzle only uses the first seven cities for that reason.

## Checking changes
- `Android CI`: unit tests, debug APK, release bundle.
- `Emulator screenshots` (`tools/ci_screens.py`): plays level 1 to the win card, resumes a saved board, visits later skies, and checks that both older save formats keep the player's level. It runs when that script or its workflow changes. Thumbnails are printed into the log as base64 (`IMG <path> <data>`).
- Debug builds use the id `com.dpjha.slidingnumbersplus.debug`, so they install beside the Play version.

## Not verified
- No real phone yet: smoothness, battery, sound levels, vibration and swipe feel are unjudged. The emulator renders in software, so its frame times mean nothing.
- Play Games has no ids, so it has never run. Real ads only load in a release build and have not been seen.
- Upgrade over the real Play install (same signing key) is untried.

## Needed from DP before release
1. Signing secrets in GitHub (`ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`). They are empty, so the CI bundle is **unsigned**. Never commit a keystore.
2. AdMob: ids are in `gradle.properties` (release only; debug uses Google test ids). Banner on every screen, interstitial every 4th win after the first city, rewarded in the shop. In the AdMob console, publish a consent message under Privacy & messaging.
3. Play Games ids in `res/values/play_games_ids.xml`. Empty ids hide the button.
4. Play Console: data safety and ads declarations must match the ads decision.

## Possible next steps
Richer art and music for cities 8 to 21, city arrival card, daily leaderboard, level-jump debug shortcut, store graphics and launch reel.

Music (DP, 27 Nov): silent outside the puzzle screen, fades in over about 2.5 s on a puzzle, and stays off if another app is already playing unless the player switches it on by hand.
