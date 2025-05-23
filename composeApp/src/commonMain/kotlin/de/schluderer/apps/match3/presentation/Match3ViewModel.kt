package de.schluderer.apps.match3.presentation

import de.schluderer.apps.match3.data.ScoreRepository
import de.schluderer.apps.match3.data.createScoreRepository
import de.schluderer.apps.match3.domain.Board
import de.schluderer.apps.match3.domain.Position
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.pow

/**
 * ViewModel for the Match-3 game.
 * Manages game state and handles user interactions.
 */
class Match3ViewModel {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val scoreRepository: ScoreRepository = createScoreRepository()

    // Default board size
    private val boardWidth = 8
    private val boardHeight = 8

    // Points for matches
    private val pointsForThreeMatch = 10
    private val pointsForFourMatch = 20
    private val cascadeMultiplier = 1.5

    // Animation durations in milliseconds
    private val swapAnimationDuration = 300L
    private val matchAnimationDuration = 300L
    private val gravityAnimationDuration = 300L
    private val fillAnimationDuration = 300L

    // UI state
    private val _uiState = MutableStateFlow(
        GameUiState(
            board = Board.createRandom(boardWidth, boardHeight)
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        // Load high score
        viewModelScope.launch {
            val highScore = scoreRepository.getHighScore()
            _uiState.value = _uiState.value.withHighScore(highScore)
        }

        // Check for initial matches and resolve them
        viewModelScope.launch {
            resolveInitialMatches()
        }
    }

    /**
     * Handles a tile tap event.
     * @param x The x-coordinate of the tapped tile.
     * @param y The y-coordinate of the tapped tile.
     */
    fun onTileTapped(x: Int, y: Int) {
        val position = Position(x, y)
        val currentState = _uiState.value

        // Ignore taps if game is over or animations are in progress
        if (currentState.gameOver || currentState.animatingTileIds.isNotEmpty()) {
            return
        }

        // If no tile is selected, select this one
        if (currentState.selectedPosition == null) {
            _uiState.value = currentState.withSelectedPosition(position)
            return
        }

        // If this tile is already selected, deselect it
        if (currentState.selectedPosition == position) {
            _uiState.value = currentState.withSelectionCleared()
            return
        }

        // If the selected tile is adjacent to this one, try to swap them
        val selectedPosition = currentState.selectedPosition
        if (selectedPosition.isAdjacentTo(position)) {
            trySwapTiles(selectedPosition, position)
        } else {
            // Otherwise, select this tile instead
            _uiState.value = currentState.withSelectedPosition(position)
        }
    }

    /**
     * Starts a new game.
     */
    fun newGame() {
        _uiState.value = GameUiState(
            board = Board.createRandom(boardWidth, boardHeight),
            highScore = _uiState.value.highScore
        )

        // Check for initial matches and resolve them
        viewModelScope.launch {
            resolveInitialMatches()
        }
    }

    /**
     * Resolves any matches that exist on the initial board.
     */
    private suspend fun resolveInitialMatches() {
        var currentBoard = _uiState.value.board
        var matches = currentBoard.findMatches()

        while (matches.isNotEmpty()) {
            // Remove matches
            currentBoard = currentBoard.removeTiles(matches)

            // Apply gravity
            currentBoard = currentBoard.applyGravity()

            // Fill empty spaces
            currentBoard = currentBoard.fillEmptySpaces()

            // Update the board
            _uiState.emit(_uiState.value.withBoardAndScore(currentBoard, 0))

            // Check for new matches
            matches = currentBoard.findMatches()
        }

        // Check if there are valid moves
        if (!currentBoard.hasValidMoves()) {
            _uiState.emit(_uiState.value.withGameOver())
        }
    }

    /**
     * Tries to swap two tiles and resolves any matches that result.
     * @param position1 The position of the first tile.
     * @param position2 The position of the second tile.
     */
    private fun trySwapTiles(position1: Position, position2: Position) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val currentBoard = currentState.board

            // Get the tiles to animate
            val tile1 = currentBoard.getTileAt(position1)
            val tile2 = currentBoard.getTileAt(position2)

            // If either tile is null, return early
            if (tile1 == null || tile2 == null) return@launch

            val tileIds = setOf(tile1.id, tile2.id)

            // Animate the swap
            _uiState.emit(currentState.withAnimatingTileIds(tileIds))
            delay(swapAnimationDuration)

            // Perform the swap using the new swapTilesById method
            val swappedBoard = currentBoard.swapTilesById(tile1.id, tile2.id)
            _uiState.emit(currentState.withBoardAndScore(swappedBoard, 0)
                .withAnimatingTileIds(emptySet()))

            // Check for matches
            val matches = swappedBoard.findMatches()

            if (matches.isEmpty()) {
                // No matches, swap back
                delay(200) // Brief pause before swapping back
                // Use swapTilesById for swapping back as well
                val originalBoard = swappedBoard.swapTilesById(tile1.id, tile2.id)
                _uiState.emit(currentState.withBoardAndScore(originalBoard, 0)
                    .withSelectionCleared())
            } else {
                // Resolve matches
                resolveMatches(matches)
            }
        }
    }

    /**
     * Resolves matches by removing matched tiles, applying gravity, and filling empty spaces.
     * @param matches The positions of tiles that form matches.
     * @param cascadeLevel The current cascade level (for scoring).
     */
    private suspend fun resolveMatches(matches: List<Position>, cascadeLevel: Int = 1) {
        val currentState = _uiState.value
        val currentBoard = currentState.board

        // Calculate score for this match
        val scoreIncrement = calculateScore(matches, cascadeLevel)

        // Get the tile IDs to animate
        val matchTileIds = matches.mapNotNull { position -> 
            currentBoard.getTileAt(position)?.id 
        }.toSet()

        // Animate the matches
        _uiState.emit(currentState.withAnimatingTileIds(matchTileIds))
        delay(matchAnimationDuration)

        // Remove matched tiles
        val boardWithoutMatches = currentBoard.removeTiles(matches)
        _uiState.emit(currentState.withBoardAndScore(boardWithoutMatches, scoreIncrement)
            .withAnimatingTileIds(emptySet()))

        // Apply gravity with animation
        val boardWithGravity = boardWithoutMatches.applyGravity()

        // Find tiles that have moved due to gravity
        val movedTileIds = boardWithGravity.tiles.filter { newTile ->
            val oldTile = boardWithoutMatches.getTileAt(Position(newTile.position.x, newTile.position.y))
            oldTile == null || oldTile.id != newTile.id
        }.map { it.id }.toSet()

        // Animate gravity
        _uiState.emit(_uiState.value.withBoardAndScore(boardWithGravity, 0)
            .withAnimatingTileIds(movedTileIds))
        delay(gravityAnimationDuration)

        // Fill empty spaces with animation
        val boardBeforeFill = boardWithGravity
        val filledBoard = boardWithGravity.fillEmptySpaces()

        // Find IDs of new tiles
        val newTileIds = filledBoard.tiles.filter { newTile ->
            boardBeforeFill.getTileAt(newTile.position) == null
        }.map { it.id }.toSet()

        // Animate new tiles
        _uiState.emit(_uiState.value.withBoardAndScore(filledBoard, 0)
            .withAnimatingTileIds(newTileIds))
        delay(fillAnimationDuration)

        // Clear animations
        _uiState.emit(_uiState.value.withAnimatingTileIds(emptySet()))

        // Check for new matches (cascades)
        val newMatches = filledBoard.findMatches()
        if (newMatches.isNotEmpty()) {
            // Resolve cascading matches with increased score multiplier
            resolveMatches(newMatches, cascadeLevel + 1)
        } else {
            // No more matches, check if there are valid moves
            if (!filledBoard.hasValidMoves()) {
                _uiState.emit(_uiState.value.withGameOver())

                // Save high score if needed
                val currentScore = _uiState.value.score
                val currentHighScore = _uiState.value.highScore
                if (currentScore > currentHighScore) {
                    viewModelScope.launch {
                        scoreRepository.saveHighScore(currentScore)
                        _uiState.emit(_uiState.value.withHighScore(currentScore))
                    }
                }
            }
        }
    }

    /**
     * Calculates the score for a match.
     * @param matches The positions of tiles that form matches.
     * @param cascadeLevel The current cascade level.
     * @return The score for the match.
     */
    private fun calculateScore(matches: List<Position>, cascadeLevel: Int): Int {
        // Group matches by rows and columns to identify match lengths
        val horizontalMatches = matches.groupBy { it.y }
        val verticalMatches = matches.groupBy { it.x }

        var score = 0

        // Score horizontal matches
        for ((_, positions) in horizontalMatches) {
            // Find consecutive positions to determine match lengths
            val sortedPositions = positions.sortedBy { it.x }
            var currentMatchLength = 1

            for (i in 1 until sortedPositions.size) {
                if (sortedPositions[i].x == sortedPositions[i - 1].x + 1) {
                    currentMatchLength++
                } else {
                    // End of a match, calculate score
                    score += scoreForMatchLength(currentMatchLength)
                    currentMatchLength = 1
                }
            }

            // Calculate score for the last match
            score += scoreForMatchLength(currentMatchLength)
        }

        // Score vertical matches
        for ((_, positions) in verticalMatches) {
            // Find consecutive positions to determine match lengths
            val sortedPositions = positions.sortedBy { it.y }
            var currentMatchLength = 1

            for (i in 1 until sortedPositions.size) {
                if (sortedPositions[i].y == sortedPositions[i - 1].y + 1) {
                    currentMatchLength++
                } else {
                    // End of a match, calculate score
                    score += scoreForMatchLength(currentMatchLength)
                    currentMatchLength = 1
                }
            }

            // Calculate score for the last match
            score += scoreForMatchLength(currentMatchLength)
        }

        // Apply cascade multiplier
        return (score * cascadeMultiplier.pow(cascadeLevel - 1)).toInt()
    }

    /**
     * Calculates the score for a match of a specific length.
     * @param length The length of the match.
     * @return The score for the match.
     */
    private fun scoreForMatchLength(length: Int): Int {
        return when {
            length < 3 -> 0
            length == 3 -> pointsForThreeMatch
            length >= 4 -> pointsForFourMatch
            else -> 0
        }
    }
}
