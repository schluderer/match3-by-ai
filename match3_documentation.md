# Match-3 Game Project Documentation

## Game Design Document (GDD)

**Game Overview:** A *match-3* puzzle game played on a fixed grid. The player swaps two adjacent tiles (by tapping or dragging) to align **three or more identical tiles** in a straight line (horizontally or vertically). When a match is formed, those tiles clear from the board and disappear. The cleared spaces are then filled by tiles falling from above, which can trigger **chain reactions** or cascades of new matches. This simple mechanic is **easy to learn** and highly accessible for touch-screen devices. We deliberately exclude any special power-up tiles or complex combos; all tiles behave uniformly. The goal is to create an engaging casual experience that encourages repeated play through progressively challenging levels or scoring modes.

* **Genre:** Casual single-player match-3 puzzle
* **Theme & Art Style:** Colorful, vibrant tiles (e.g. candies, gems, fruits) on a clean background. Clear visual distinction of tile types (shapes/colors) for quick recognition.
* **Target Platforms:** Android, iOS, desktop (cross-platform via Kotlin Multiplatform). No external controllers – input by taps or drags on touch screens and mouse/keyboard on desktop.

**Grid Layout & Accessibility:** Use a **square grid**, with dimensions chosen for mobile ergonomics. A **6×6 or 8×8 grid** is recommended so that tiles remain large enough for accurate tapping on phones. This moderate grid size balances challenge with ease of use; for example, Candy Crush’s standard grids are around 9×9, but for simplicity we use a slightly smaller fixed grid. The grid should be centered on the screen with ample padding so that all tiles are easily visible without crowding.

**Core Mechanics:**

* **Tile Swapping:** The player selects (taps) one tile and then an adjacent tile to swap their positions. (As an alternative, the player can drag a tile onto an adjacent tile to swap them.) Only horizontal or vertical swaps between adjacent tiles are allowed.
* **Matching:** After a swap, if **three or more like tiles align** in a row or column, it counts as a match. For example, swapping two adjacent red tiles to form a horizontal line of three red tiles creates a match. If no match is formed, the swap is reverted (invalid swap).
* **Clearing:** Matched tiles **vanish** from the board, awarding points. Immediately after clearing, any tiles above fall down (gravity) to fill the empty spaces. New random tiles enter from the top to fill the board back to full. This falling action can create **chain reactions**: new matches that automatically form as tiles settle.
* **Scoring:** Each match yields points (e.g. 3 tiles = 10 points, 4 tiles = 20 points, etc.). Bonus points can be awarded for chain reactions. The score is displayed prominently.

**Visual & Interaction Design:**
*Figure: A typical match-3 game on mobile with colorful tiles (example Candy Crush UI).* The interface features a **grid of colorful tiles**. Each tile uses a distinct shape or icon (and color) to be easily distinguishable. The UI outside the grid is minimal: a header or footer showing the score, level goals, and moves/remaining-time counter. Use **bright, high-contrast colors** so players can quickly identify tile types.

Animations and effects are **simple and smooth**. When two tiles swap, animate them sliding into each other. When tiles clear, they can **fade out or shrink**, possibly with a brief sparkle or flash effect to reinforce the match. Tiles falling down should move in a fluid, gravity-like animation. These animations provide immediate feedback so the player clearly sees what happened (swap success, tiles disappearing, etc.). For example, a tapped tile might highlight briefly or show a subtle glow to indicate selection. No complex particle systems are needed – just basic motion and fade effects suffice.

For input, prioritize **touchscreen friendliness**. Players tap one tile then tap an adjacent tile to swap; alternatively, dragging one tile over its neighbor should also trigger a swap. Controls should be intuitive with minimal learning curve: *“tap or drag to swap adjacent tiles”*. There are no menus or dialogs in active play, so all interactions happen directly on the grid or via simple buttons (like a back or pause button). Multi-touch isn’t required. The interface should run at a consistent frame rate with no lag, as smooth play is crucial for quick reactions. All tiles and UI elements should be sized so that they are easily tapped on a phone screen (roughly at least 40–50 px on mobile screens).

**Win/Lose Conditions:**

* **Endless Mode (High Score):** The player continues making matches until no more legal swaps remain (or optionally until a timer runs out). There is no fixed goal; the aim is to achieve the highest score possible. The game ends when the board is locked (no match-3 can be made) or moves expire. This mode is “lose-on-run-out-of-moves.” Endless mode appeals to casual play and replayability.

## Technical Guideline Document

**Overview & Architecture:** Use a **clean, modular architecture** (e.g. MVVM combined with Clean Architecture principles) to maximize code reuse across platforms and isolate UI from game logic. Kotlin Multiplatform (KMP) allows us to share almost all game logic: the model of the board, matching algorithms, and game state management will live in a shared module (`commonMain`). Platform-specific code (UI rendering, input capture, sound) will reside in the Android, iOS, and desktop modules. This separation ensures **maintainability and testability**: core gameplay (the “business logic”) is platform-agnostic and can be tested on the JVM, while UI layers on each platform handle presentation and user events.

A **layered structure** is recommended:

* **Domain Layer (common):** Defines game entities (e.g. `Tile`, `Board`, enums for tile types) and core game rules. Contains algorithms for detecting matches, computing cascades, scoring, and tracking state (score, moves, level). Use pure Kotlin without any platform-specific APIs.
* **Data/Logic Layer (common):** If needed, separate some logic or data providers. For a simple game, domain and data may merge. But for Clean Architecture, this layer could handle persistence (e.g. saving high scores) or resource loading (using `expect/actual` as needed for things like file IO or asset loading).
* **Presentation/ViewModel Layer (common):** Implements the MVVM pattern. A shared ViewModel (or similar state-holder) exposes the game state (board layout, score, moves left) as observable state (for example, as a Kotlin **StateFlow** or LiveData). The ViewModel receives input events (e.g. “tile at (x,y) tapped”) and updates the model accordingly. The ViewModel runs the game loop (updating gravity, checking wins, etc.) and emits updated UI state. Using a common ViewModel lets us reuse presentation logic: both Android Compose and iOS SwiftUI views can observe the same state model.

Example:

```kotlin
// In commonMain:
class Match3ViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState(...))
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    // handle input from UI:
    fun onTileTapped(x: Int, y: Int) { /* update state & emit new GameUiState */ }
    // game update loop could be here (e.g., using viewModelScope)
}
```

In KMP, you can use the multiplatform `ViewModel` (from Jetpack’s lifecycle library) in common code. This ViewModel will hold an instance of game state and logic.

**Module Boundaries:** Structure the project with clear source sets. For example:

```
shared/              # common code (kotlin source sets)
 ├─ domain/          # game entities, models
 ├─ data/            # (optional) repositories or logic components
 ├─ presentation/    # shared ViewModels, UI state models
 ├─ utils/           # shared utilities
androidApp/          # Android-specific module (UI, activities)
iosApp/              # iOS-specific module (UI, App delegate)
desktopApp/          # Desktop-specific module (e.g. Compose Desktop UI)
```

Put *all* core logic, models, and the ViewModel into `shared/commonMain`. Platform modules depend on `shared` and contain only UI code and glue. For instance, `androidApp/MainActivity.kt` would set up a Compose UI that binds to `Match3ViewModel` from common code. The same `Match3ViewModel` class would be instantiated in the iOS app (using a SwiftUI-compatible lifecycle) and in the desktop app. This maximizes code sharing.

**Platform-Specific Abstraction:** Avoid using platform-specific APIs in shared code. For example, Kotlin’s `kotlinx.coroutines` is available on all targets and can be used in common code for asynchronous tasks. However, if you need a platform-specific feature (say, loading a JSON level file, playing a sound, or accessing device vibration), use Kotlin’s `expect/actual` mechanism: declare an `expect fun loadSound(name: String): Sound` in common code, and provide `actual` implementations on Android (using Android sound APIs) and iOS. Keep these abstractions minimal. Remember: common code cannot import Java/Android or iOS frameworks directly. If you use any library (e.g. a random number generator, serialization), prefer multiplatform libraries (like kotlinx.serialization or kotlinx.coroutines) that support all targets.

**State Management:** Treat the game state (board grid, score, moves) as a single source of truth in the shared ViewModel. Use **immutable data structures or state-flows** to update it atomically. For example, each time the player swaps tiles or tiles fall, produce a new `GameState` object reflecting the updated grid and score, and emit it via `StateFlow`. Compose or SwiftUI views can collect/observe this flow to re-render the UI. This reactive pattern avoids manual UI updates. If using Compose on Android/desktop, you might use `collectAsState()` on the `StateFlow`; on iOS with SwiftUI, you can use the interop support to observe a shared Flow or use Combine. Ensure updates happen on the main/UI thread for safe consumption.

**Game Loop & Concurrency:** Implement the game’s update loop in a coroutine (in the ViewModel). For instance, after a swap, run a loop that repeatedly:

1. Check for matches;
2. Remove matched tiles;
3. Let tiles fall and refill;
4. Pause briefly to animate;
5. Repeat until no new matches form.
   Use `delay(...)` or frame-synced timers to pace animations. Coroutines with `viewModelScope` work on Android/desktop (note: add `kotlinx-coroutines-swing` on desktop to support `Dispatchers.Main`). On iOS, coroutines and main dispatch should interoperate via Kotlin/Native’s dispatchers.

Be mindful of threading differences: Android often uses a separate **Render thread** for continuous drawing, while iOS might use the main thread. According to real-world experiences, game variables should be confined to the render thread to avoid needing volatile flags. On iOS (Kotlin/Native), avoid mutable shared state between threads; prefer immutable data classes. For example, define touch or swap events as immutable data (`data class TouchEvent(val x: Int, val y: Int)`) and pass them to the render/update coroutine. This prevents concurrency issues: Kotlin/Native doesn’t allow `@Volatile` on objects, so immutable event passing is the recommended pattern. In practice, on each frame tick, apply any pending user input (from the UI thread) and then update the game state on the render/update thread.

**Input Handling:** On each platform, capture user gestures and forward them to the shared logic. For example, in Android Compose `onTap` or `onDrag` handlers call `viewModel.onTileTapped(x,y)`. On iOS SwiftUI, use a `UIViewRepresentable` or native gesture recognizer that invokes a Kotlin function in the ViewModel. Convert touch coordinates to tile indices before sending them. The shared ViewModel then resolves which swap (if any) that tap implies and updates the grid. Keep the UI code thin: it only converts UI events to ViewModel calls and renders the latest state.

**Error Handling & Edge Cases:** The shared logic should handle illegal swaps (i.e., swaps that don’t produce a match) by reverting the swap. It should also detect stalemate (no possible matches left) and signal game over in endless mode. Any alerts or messages (“Game Over”, “Level Complete”) can be communicated via state flags (in `GameUiState`) that the UI observes and shows accordingly.

**Performance:** Although the game is simple, ensure smooth performance. Since we're running on mobile, avoid heavy loops on the main thread. Use coroutines for asynchronous work. Keep the tile grid small enough that match detection (O(n) checks per swap) is trivial. For visual performance, use Compose’s or SwiftUI’s efficient invalidation: update only when the state changes.

## Code Style Guide

We follow **official Kotlin conventions** and consistent project structure to make the code clear and maintainable.

* **Project Structure:** Align with the module plan above. In the **shared** module, organize by feature or layer: e.g. a `model` package with data classes (`Tile`, `Position`), a `logic` or `usecase` package with game rules, and a `viewmodel` package with state holders. Platform projects (androidApp, iosApp, desktopApp) contain UI code. File names should match class names: if a `.kt` file has one top-level class, name it `MyClass.kt`. If a file holds multiple related declarations (e.g. extension functions), use a descriptive plural name like `Extensions.kt`.

* **Naming Conventions:**

    * **Classes/Types:** Use **PascalCase**. For example, `class GameBoard`, `enum class TileColor`. Composable functions in Jetpack Compose (returning `@Composable`) use PascalCase like `GameBoardView`.
    * **Functions & Variables:** Use **camelCase**. Functions are usually verbs or verb-phrases (e.g. `swapTiles()`, `checkForMatches()`). Properties (vals/vars) are nouns (e.g. `tiles`, `moveCount`) also in camelCase. Constants (`const val`) use **UPPER\_SNAKE\_CASE** (e.g. `const val MAX_MOVES = 20`). Do not use special characters or spaces in identifiers (only ASCII letters/digits and `_` for constants).
    * **Packages and Modules:** Use all-lowercase names, typically matching the domain. For example, `com.example.match3game` for the root package. Within shared code, you might have `com.example.match3game.domain`, `com.example.match3game.presentation`, etc.

* **Formatting:**

    * **Indentation:** Use **4 spaces** for each indent level; do not use tabs.
    * **Braces:** Use K\&R style (Egyptian braces) for non-empty blocks: put the opening `{` at the end of the preceding line, and closing `}` aligned with the start of the statement (no extra newline before brace). Example:

      ```kotlin
      if (score >= targetScore) {
          // do something
      } else {
          // another branch
      }
      ```

      **Braces are required** for `if`, `for`, `when` branches, `do/while`, even for single statements. (Single-line `if` or `when` with no `else` may omit braces if it fits one line.)
    * **Line Length:** Keep lines reasonably short (around 100 characters) for readability.
    * **Blank Lines:** Use blank lines sparingly to separate logical sections. For example, one blank line between functions or between `import` statements and class definitions. Do not use multiple consecutive blank lines.
    * **Unicode:** Use UTF-8 encoding for all files. Avoid non-ASCII characters in code except in string literals (escape them if needed).

* **Documentation:** Use Kotlin’s **KDoc** style (`/** ... */`) for all public classes, functions, and complex logic. Start each KDoc comment with a **brief summary sentence**, then details. For example:

  ```kotlin
  /**
   * Represents the game board of size width×height with a grid of tiles.
   * Provides methods to swap tiles and find matches.
   */
  class GameBoard(val width: Int, val height: Int) { ... }
  ```

  Every public class and its public members should have KDoc, even if it's just one line summarizing its purpose. This helps maintain clarity, especially in shared code that will be used by multiple platforms. Inline comments (`//`) can be used for short clarifications within functions, but avoid redundant comments on obvious code.

* **Dependencies and Imports:** In common code, prefer Kotlin standard library and **Kotlinx** libraries (coroutines, serialization). In platform code, use platform-specific UI libs (Jetpack Compose on Android/desktop, SwiftUI/UIKit on iOS). Keep the number of dependencies minimal. For example, if using MVVM, you might include `lifecycle-viewmodel-compose` in common (as shown in \[42†L29-L33]) to reuse `ViewModel`. Always keep `import` lists clean (Android Studio/IntelliJ can organize imports automatically).

* **Cross-Platform Code Separation:**

    * Shared logic and models go in `commonMain` sources. Do **not** put any Android or iOS view code here.
    * Use `expect/actual` only when necessary (e.g., for file I/O, platform-specific resources, or APIs like timer triggers). Keep the common API minimal.
    * Keep UI code in each platform’s source set. For example, define the Android UI (Compose) under `androidApp/src/androidMain/kotlin/...`, and an iOS SwiftUI UI under `iosApp/...`. Both UIs will interact with the same shared ViewModel APIs.
    * Follow idiomatic code style on each platform as well: Android and desktop can use Compose code style (e.g. use trailing lambdas, no semicolons), iOS Swift code (in shared Kotlin, use Kotlin style as above). The shared code should not have any `PlatformXxx` methods – it should use pure Kotlin.

* **Formatting Tools:** Optionally, configure `ktlint` or a Kotlin formatter to enforce style, or set up an `.editorconfig` for consistent indentation. The Android/Kotlin style guide is the reference.

* **Examples:**

    * Good class and function naming: `class Tile(val color: TileColor) { ... }` and `fun swapTiles(x1: Int, y1: Int, x2: Int, y2: Int) { ... }`.
    * File naming: `GameBoard.kt` contains `class GameBoard`, `Tile.kt` contains `enum class TileColor`.
    * Constants: `const val MIN_MATCH = 3` (in UPPER\_SNAKE\_CASE) for the minimum number of tiles to match.
    * Documentation example:

      ```kotlin
      /**
       * Checks the board for any matches of 3 or more tiles in a row or column.
       * @return a list of tile positions that form matches.
       */
      fun findMatches(): List<Position> { ... }
      ```

* **Testing Code (if any):** Follow the same style. Test functions (in test source sets) can use underscores in names if needed for clarity (the style guide allows it in test names).

By adhering to these guidelines—clean architecture separation, consistent naming/formats, and comprehensive documentation—the codebase will remain organized and easy to maintain across all supported platforms.

**Sources:** Official Kotlin Multiplatform and style guidelines, and best practices in match-3 game design.
