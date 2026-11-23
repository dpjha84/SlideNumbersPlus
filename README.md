# SlidePulse — Android Sliding Number Puzzle

An Android-only tactile sliding number puzzle game (`com.slidepulse.puzzle`) built with React, TypeScript, Tailwind CSS, Motion, and Capacitor Android.

## Features
- **Escalating Puzzle Grids**: `3×3` Warm-Up, `4×4` Tactical, `5×5` Master, and `6×6` Grandmaster levels with guaranteed-solvable seeded scrambles.
- **Tactile Hand Feel**: Spring-bounce animations, multi-tile row/column slides, swipe gestures, theme-matched Web Audio FX, and native `@capacitor/haptics` feedback.
- **Target Moves & Star Rating**: Earn 1–3 Stars per level based on moves taken against par.
- **Currencies & Workshop Store**: Earn Stars, Diamonds, and Coins to unlock board themes (**City Skyline**, **Neon Cyberpunk**, **Alpine Timber**, **Arctic Marble**, **Obsidian Gold**), tile skins, and power-ups (**Hint Magnet**, **Step Rewind**, **Tile Swap**, **2× Coin Booster**).
- **Daily Challenges & Global Leaderboards**: Date-seeded competitive daily grid and ranked global standings.
- **Google Play Games & AdMob**: Native Play Games Services v2 plugin (`plugins/capacitor-play-games`) and `@capacitor-community/admob` (`src/utils/admob.ts`) with always-visible banner ads and a 2-minute interstitial cooldown.

## Project Structure
- `.github/workflows/android.yml` — Automated two-pass GitHub Actions CI building Debug APK, Release Test APK, and Play Store AAB
- `android/` — Native Capacitor Android project (`keystore.properties` signing + `admobAppId` manifest placeholder)
- `plugins/capacitor-play-games/` — Local Play Games Services v2 Capacitor plugin
- `src/utils/` — Native `admob.ts`, `playGames.ts`, and `feedback.ts` services
- `src/` — Puzzle engine, themes, and tactile UI components
- `capacitor.config.ts` — Capacitor Android configuration

## Android build (GitHub Actions)
Push this repo to GitHub → **Actions** → **Android build** → **Run workflow**.
Download `slidepulse-debug-apk` to sideload and test directly on your Android phone, and `slidepulse-release-aab` for Google Play Console.

### Signing for Play Store AAB & Stable Play Games SHA-1
Add repository secrets under **Settings → Secrets and variables → Actions**:
- `UPLOAD_KEYSTORE_BASE64`: Base64 of your `.jks` upload keystore (`base64 -w 0 upload.jks`)
- `KEYSTORE_PASSWORD`: Keystore password
- `KEY_ALIAS`: Key alias
- `KEY_PASSWORD`: Key password
- Optional AdMob production overrides: `VITE_ADMOB_BANNER_UNIT_ID`, `VITE_ADMOB_INTERSTITIAL_UNIT_ID`, `VITE_ADMOB_REWARDED_UNIT_ID`

Tagging a commit (`git tag v1.0.0 && git push --tags`) attaches APK and AAB artifacts directly to a GitHub Release.

## Local Build
```bash
npm install && npm run build && npx cap sync android && cd android && ./gradlew bundleRelease
```
