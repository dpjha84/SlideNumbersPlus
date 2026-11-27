# Sliding Numbers+ 3.0 — handoff for a fresh session

Read this first. It carries the full context from the earlier session so work can continue in a new one.

## Who and what
- Owner: DP (Divyaprakash Jha), indie studio **Pixel Pocket** (Instagram @pixelpocket.apps).
- Goal: rebuild **Sliding Numbers+** (Play listing "Sliding Numbers+: City Puzzle", package `com.dpjha.slidingnumbersplus`) into a game that is **almost identical to his other game, ColorSort, in every system and in polish, except for the core gameplay**. Top-notch graphics, look and feel, and effects. No bugs, no regressions, nothing that damages trust.
- Upgrade the existing Play listing (same package, same signing). **versionCode starts at 300**, versionName 3.0.0.
- Repo: `dpjha84/SlideNumbersPlus` (this repo). Work is on branch `v3-rewrite`; `main` still holds an old unrelated React project (SlidePulse) and was deliberately wiped on the branch at DP's instruction. Old native app for reference: `dpjha84/SlidingNumbersPlusV2` (read-only clone, has Play Games and AdMob service code, 3 city images, a 2^size move limit, hints).
- The Claude GitHub app is installed for this repo, so pushes work. CI (GitHub Actions) compiles and runs tests on every push to any branch; there is **no Android SDK here**, so CI is the only compiler. Always push and read the CI result before claiming something works.

## Status: DP tested the current build on a phone and is NOT happy
He said it is "not anywhere close to my expectation". He did not list specifics. Treat the current UI as a rough prototype and aim much higher. Ask him what felt wrong (look, feel, speed, missing screens), then raise the bar across the board. What exists now:
- `game/PuzzleEngine.kt`: rectangular boards, chain slides, always-solvable scramble, unit tested.
- `game/Levels.kt`: city every 5 levels, size ladder, tours, par, stars, unit tested.
- `ui/BoardState.kt` + `ui/BoardView.kt`: drag-follow tiles, swipe commits at 20% of a cell, very stiff critically damped spring, glass translucent colourful tiles, glow when in place, haptics.
- `ui/art/*`: Canvas city backdrops (3 skyline layers with haze and parallax sway, water reflection, stars, clouds, birds, boats, blinking windows, Paris searchlight), 21 landmarks, day/sunset/night palettes. The 14 non-previewed landmarks are simple first drafts.
- `ui/GameScreen.kt`: bare screen. No home screen, no saving, no economy. versionCode 300.
- None of the ColorSort systems below exist yet.

## Decisions DP has made (do not re-ask)
- A **new city every 5 levels**. Board grows along a ladder of rectangular steps: 3×3, 3×4, 4×4, 4×5, 5×5, 5×6, 6×6 (cap). 7 cities = 35 levels = one **World Tour**. Later tours reuse the ladder with new cities and tighter move limits; cities return in **Night and Sunset looks**. About 21 cities in the roster (see `Levels.cities`).
- **Postcard mode (picture-slice tiles) is dropped entirely.** City theme comes from illustrated skylines with depth and life instead.
- Tiles must feel like **solid tiles**, not jelly (no squash and stretch). Drag follows the finger. **Movement must be extremely fast** ("don't waste a millisecond"); a swipe commits almost immediately. Tiles are **colourful, slightly translucent**, and tiles in the right place show in a **different, brighter state**. Consider a "Fast moves" setting.
- Mock values that he approved the feel of (web mock in `docs/design/motion.html`, built by `mk.py`): spring stiffness 10000, damping 200, ripple 0 ms, commit at 20% of a cell, lift scale about 1.03, bump (one short nudge) for blocked tiles, hue by home position from cyan 175° to amber 360°.
- Moves: replace the old 2^size move limit with a **par** (shuffle depth / Manhattan based); stars by moves vs par.

## Systems to carry over from ColorSort (reference implementation: repo `dpjha84/color-sort-puzzle`, React/Vite/Capacitor)
Match behaviour and polish, in native Kotlin/Compose:
- **Home screen** with hero art, resume, daily card, streak, level/tour progress (see `src/components/HomeScreen.tsx`).
- **Daily Challenge** + **streaks**, with streak freeze, milestone chests at 3/7/14/30 days (planned for ColorSort 1.0.8; build here from the start). Open question for DP: daily reset in local time vs UTC.
- **Play Games Services v2**: global and daily **leaderboards**, **achievements**, **cloud save via Saved Games** (ColorSort uses snapshot `colorsort_save_v1`; merge by rank, keep local looks). Sign-in is optional; the game must work fully without Play Games (DP asked about this). IDs must be created in Play Console for this app.
- **Economy**: coins, gems, XP, hints, undo, extra moves, shop, rewarded ads (AdMob), banner ads.
- **Resume board** (save in-progress board), **review prompt**, **native share**, **themes/skins** (tile styles), **My Stats** screen, rewards modal, victory/failed modals, level-select modal, ambient background, audio (see `src/utils/audio.ts`), confetti/burst effects (`Burst.tsx`).
- Players on an older build must resume at their old level (ColorSort verified this; here the old app's progress lives in DataStore, so migrate it).
- Hints: optimal search is practical on 3×3 and 3×4; larger boards use a distance heuristic hint.

## Quality bar and process
- Effects: satisfying solve celebration (tiles wave, skyline lights up), city arrival card on each new city, passport/stamp collection screen, particle bursts, polished transitions, sound and haptics. Think about every screen, not just the board.
- Also still to design: home-screen hero skyline, tile skins (glass, neon, wood, marble, brick, stone), app icon options, Play feature graphic 1024×500, Play listing text, Instagram reel for launch.
- Test: unit tests for all game logic; consider Compose UI tests; add a debug level-jump shortcut for reviewing cities.
- Design assets and web preview tools in `docs/design/` (`sky2.py` generates SVG skylines, `common.js`/`mk.py` the mocks). Plan was to convert final art to Compose Canvas or VectorDrawables.
- Previews can be built as HTML/Playwright mocks and published as Artifacts so DP can feel them on his phone before porting to Compose. DP likes to see previews before implementation.
- Keep a plan doc (Claude Docs "Sliding Numbers+ 3.0 plan") updated; it is stale (still lists Postcard mode).

## Open questions for DP
1. What specifically fell short on the phone test (look, feel, speed, art, missing screens)?
2. Daily reset: local time or UTC?
3. Which Play Games IDs exist already for this listing?
4. Tile skins, sound style, and whether to include a third "empty" mechanic (ColorSort idea) — not applicable here, ignore.
