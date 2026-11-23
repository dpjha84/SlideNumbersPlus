# SlidePulse — Android Sliding Number Puzzle

An Android-only tactile sliding number puzzle game built with React, TypeScript, Tailwind CSS, Motion, and Capacitor Android.

## Features
- **Escalating Puzzle Grids**: `3×3` Warm-Up, `4×4` Tactical, `5×5` Master, and `6×6` Grandmaster levels with guaranteed-solvable seeded scrambles.
- **Tactile Hand Feel**: Spring-bounce animations, multi-tile row/column slides, swipe gestures, theme-matched Web Audio FX, and native `@capacitor/haptics` feedback.
- **Target Moves & Star Rating**: Earn 1–3 Stars per level based on moves taken against par.
- **Currencies & Workshop Store**: Earn Stars, Diamonds, and Coins to unlock board themes (**City Skyline**, **Neon Cyberpunk**, **Alpine Timber**, **Arctic Marble**, **Obsidian Gold**), tile skins, and power-ups (**Hint Magnet**, **Step Rewind**, **Tile Swap**, **2× Coin Booster**).
- **Daily Challenges & Global Leaderboards**: Date-seeded competitive daily grid and ranked global standings.
- **Google Play Games & AdMob**: Native Play Games Services v2 plugin (`plugins/capacitor-play-games`) and `@capacitor-community/admob` integration with persistent banner ads and a 2-minute interstitial cooldown.

## Project Structure
- `.github/workflows/build-apk.yml` — Automated CI workflow to build `app-debug.apk`
- `android/` — Native Capacitor Android project
- `plugins/capacitor-play-games/` — Local Play Games Services v2 Capacitor plugin
- `src/` — Game engine, themes, UI components, and Capacitor services
- `capacitor.config.ts` — Capacitor Android configuration

## Building the APK
### Option A: Automated via GitHub Actions
Push this repository to GitHub and run the **Build Android APK** workflow under the **Actions** tab to download `SlidePulse-debug-apk`.

### Option B: Locally in Android Studio
```bash
npm install
npm run cap:android
```
Then select **Build → Build Bundle(s) / APK(s) → Build APK(s)** in Android Studio.
