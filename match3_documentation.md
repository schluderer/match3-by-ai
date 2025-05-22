## 1. Game Design Document (GDD)

**Game Overview**
A classic single-player Match-3 puzzle on a fixed grid. Players swap two adjacent tiles (by tap or drag) to form lines of ≥ 3 identical tiles; matches clear, points are awarded, tiles fall to refill, and cascades may occur. No special/power-up tiles—pure, accessible mechanics.

* **Genre:** Casual puzzle, single-player
* **Art Style:** Bright, high-contrast shapes/colors on a clean background—tiles sized for clear recognition on small screens.
* **Platforms:** Android, iOS, Desktop — UI entirely in Compose Multiplatform.

**Grid & Accessibility**

* **Grid Size:** 6×6 or 8×8 fixed grid.
* **Tile Size:** Minimum \~40–50 px tap area on phones; grid centered with padding.

**Core Mechanics**

* **Swap:** Tap one tile then an adjacent tile (or drag one onto its neighbor). Only horizontal/vertical swaps.
* **Match:** 3+ identical tiles aligned horizontally or vertically.
* **Clear & Refill:** Matched tiles disappear; above tiles fall down; new tiles spawn at top. Chain reactions auto-resolve.
* **Scoring:**

  * 3-tile match → base points (e.g. 10)
  * 4-tile match → higher points (e.g. 20)
  * Cascades → bonus multipliers

**Visual & Interaction Design**

* **UI:** Tile grid + top bar showing score and moves/remaining time. Minimal chrome.
* **Animations:**

  * Swap → sliding motion.
  * Clear → fade/shrink with brief sparkle.
  * Refill → gravity-style drop.
  * Tap highlight → subtle glow.
* **Controls:** Touch (tap/drag) on mobile; mouse click/drag on desktop. All in Compose MP gestures.

**Game Modes**

1. **Endless Mode (High Score):** Play until no valid swaps remain (or optional timer). Aim for max points.
2. **Level Mode (Target):** Predefined levels with goals (e.g. reach X points or clear N tiles in M moves). Win to advance or retry on failure. Difficulty ramps by raising targets or introducing more tile colors.

---

## 2. Technical Guideline Document

### Architecture & Modules

Adopt **Clean Architecture + MVVM** in shared code, with UIs in **Compose Multiplatform**:

```
shared/                   # commonMain & commonTest
 ├─ domain/               # game entities & rules (Tile, Board, Match detection)
 ├─ data/                 # persistence (high scores), resource loading via expect/actual
 ├─ presentation/         # ViewModels & UI-state models
 └─ ui/                   # Compose Multiplatform UI components (shared)
androidApp/               # Android entrypoint
iosApp/                   # iOS entrypoint (Compose for iOS)
desktopApp/               # Desktop entrypoint (Compose for Desktop)
```

* **commonMain** holds all game logic, ViewModels and Compose UI definitions.
* Each platform module only wires up the Compose runtime (Activity, AppDelegate, Window).

### Shared ViewModel & State

```kotlin
class Match3ViewModel : ViewModel() {
  private val _uiState = MutableStateFlow(GameUiState(...))
  val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

  fun onTileTapped(x: Int, y: Int) { /* update & emit new state */ }
  // Handles match detection, clearing, refill, scoring, cascades.
}
```

* Use `kotlinx.coroutines` + `viewModelScope` for game loop (swap→match→refill→animate).
* Expose UI-state as immutable `StateFlow` for Compose `collectAsState()`.

### Compose Multiplatform UI

* **Shared Composables** (in `shared/ui`):

  * `@Composable fun GameBoard(uiState: GameUiState, onTileTap: (Int,Int)->Unit)`
  * `@Composable fun ScoreBar(score: Int, movesLeft: Int)`
* **Platform Setup**:

  * **Android**: `MainActivity` sets `setContent { Match3App() }`
  * **iOS**: `AppDelegate` hosts a ComposeWindow via Compose for iOS
  * **Desktop**: `main()` opens Compose Desktop window

All gesture handling (`Modifier.clickable`, `pointerInput`) and drawing (`Canvas`, `Image`, `Box`) lives in shared Compose code—no SwiftUI or UIKit.

### expect/actual Abstractions

Use only for truly platform-specific needs (e.g. file I/O or sounds):

```kotlin
// commonMain
expect fun loadSound(name: String): SoundPlayer

// androidMain
actual fun loadSound(name: String): SoundPlayer = AndroidSoundPlayer(name)

// iosMain
actual fun loadSound(name: String): SoundPlayer = IosSoundPlayer(name)
```

Keep these minimal; core logic has zero platform dependencies.

---

## 3. Code Style Guide

Based on **Official Kotlin Style** + Compose MP best practices:

1. **Project Structure**

  * `shared/` for all Kotlin code (logic, ViewModels, Composables).
  * `androidApp/`, `iosApp/`, `desktopApp/` only for application bootstrap.

2. **Naming**

  * **Classes/Composables:** PascalCase.

    * e.g. `GameBoard`, `Match3ViewModel`, `ScoreBar`
  * **Functions/Properties:** camelCase.

    * e.g. `fun findMatches()`, `val tileColors: List<TileColor>`
  * **Constants:** UPPER\_SNAKE\_CASE.

    * e.g. `const val MIN_MATCH = 3`

3. **Formatting**

  * 4-space indents, K\&R braces.
  * Max \~100 chars/line.
  * Single blank line between functions/classes.
  * KDoc for all public APIs:

    ```kotlin
    /**
     * Detects all matches of length ≥ MIN_MATCH on the current board.
     * @return list of tile positions forming matches.
     */
    fun findMatches(): List<Position> { … }
    ```

4. **Compose-Specific**

  * Composables use trailing lambdas and no semicolons.
  * Place `@Composable` functions in `shared/ui/...`.
  * Use `collectAsState()` on `StateFlow` within Composables.
  * Keep Composables stateless; all logic in ViewModel.

5. **Coroutines & Concurrency**

  * Use `viewModelScope.launch { … }` for async.
  * Rely on immutable UI-state and `StateFlow` for thread safety.

6. **Dependency Management**

  * Shared: `kotlinx-coroutines`, `org.jetbrains.compose.runtime`.
  * Android: `androidx.activity:activity-compose`.
  * iOS: Compose for iOS runtime.
  * Desktop: `org.jetbrains.compose.desktop:desktop`.

7. **Linting/Formatting Tools**

  * Configure `ktlint` or IntelliJ auto-formatter.
  * Enable the Android/Kotlin style rules in CI.

---

With these updates, **every UI view** is built in **Compose Multiplatform**, maximizing code reuse and a unified declarative UI approach across Android, iOS, and Desktop.
