# Sliding Numbers+

Native Android (Kotlin, Jetpack Compose) city-themed sliding puzzle by Pixel Pocket.
Package `com.dpjha.slidingnumbersplus`, versionCode 300.

- A new city every 5 levels. The board grows along 3×3, 3×4, 4×4, 4×5, 5×5, 5×6, 6×6, then a new tour begins.
- Very fast glass tiles that glow when they are in the right place.
- `game/` holds the pure Kotlin engine and level rules (unit tested). `ui/` holds the Compose board.

CI (`.github/workflows/android.yml`) runs unit tests and builds on every push.
