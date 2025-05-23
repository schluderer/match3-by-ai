package de.schluderer.apps.match3.presentation

import de.schluderer.apps.match3.domain.Board
import de.schluderer.apps.match3.domain.Position
import de.schluderer.apps.match3.domain.Tile

/**
 * Represents the UI state of the game.
 * @property board The current game board.
 * @property score The current score.
 * @property highScore The current high score.
 * @property selectedPosition The currently selected tile position, if any.
 * @property gameOver Whether the game is over (no more valid moves).
 * @property animatingTileIds IDs of tiles that are currently being animated.
 */
data class GameUiState(
    val board: Board,
    val score: Int = 0,
    val highScore: Int = 0,
    val selectedPosition: Position? = null,
    val gameOver: Boolean = false,
    val animatingTileIds: Set<Int> = emptySet()
) {
    /**
     * Gets all tiles on the board.
     * @return A list of all tiles on the board.
     */
    val tiles: List<Tile>
        get() = board.tiles

    /**
     * Checks if a tile at the given position is currently being animated.
     * @param position The position to check.
     * @return True if the tile is being animated, false otherwise.
     */
    fun isAnimating(position: Position): Boolean {
        val tile = board.getTileAt(position)
        return tile != null && animatingTileIds.contains(tile.id)
    }

    /**
     * Finds a tile by its ID and checks if it's currently being animated.
     * @param id The ID of the tile to check.
     * @return The tile if found and it's being animated, null otherwise.
     */
    fun findAnimatingTileById(id: Int): Tile? {
        if (!isAnimating(id)) return null
        return board.findTileById(id)
    }

    /**
     * Checks if a tile with the given ID is currently being animated.
     * @param tileId The ID of the tile to check.
     * @return True if the tile is being animated, false otherwise.
     */
    fun isAnimating(tileId: Int): Boolean {
        return animatingTileIds.contains(tileId)
    }

    /**
     * Checks if a tile at the given position is currently selected.
     * @param position The position to check.
     * @return True if the tile is selected, false otherwise.
     */
    fun isSelected(position: Position): Boolean {
        return selectedPosition == position
    }

    /**
     * Creates a copy of this state with a new board and incremented score.
     * @param newBoard The new board.
     * @param scoreIncrement The amount to increment the score by.
     * @return A new GameUiState with the updated board and score.
     */
    fun withBoardAndScore(newBoard: Board, scoreIncrement: Int): GameUiState {
        return copy(
            board = newBoard,
            score = score + scoreIncrement,
            selectedPosition = null
        )
    }

    /**
     * Creates a copy of this state with the given position selected.
     * @param position The position to select.
     * @return A new GameUiState with the position selected.
     */
    fun withSelectedPosition(position: Position): GameUiState {
        return copy(selectedPosition = position)
    }

    /**
     * Creates a copy of this state with the selection cleared.
     * @return A new GameUiState with no selected position.
     */
    fun withSelectionCleared(): GameUiState {
        return copy(selectedPosition = null)
    }

    /**
     * Creates a copy of this state with the given positions marked as animating.
     * @param positions The positions to mark as animating.
     * @return A new GameUiState with the positions marked as animating.
     */
    fun withAnimatingPositions(positions: Set<Position>): GameUiState {
        val tileIds = positions.mapNotNull { position -> 
            board.getTileAt(position)?.id 
        }.toSet()
        return copy(animatingTileIds = tileIds)
    }

    /**
     * Creates a copy of this state with the given tile IDs marked as animating.
     * @param tileIds The IDs of tiles to mark as animating.
     * @return A new GameUiState with the tile IDs marked as animating.
     */
    fun withAnimatingTileIds(tileIds: Set<Int>): GameUiState {
        return copy(animatingTileIds = tileIds)
    }

    /**
     * Creates a copy of this state with the game marked as over.
     * @return A new GameUiState with gameOver set to true.
     */
    fun withGameOver(): GameUiState {
        return copy(gameOver = true)
    }

    /**
     * Creates a copy of this state with the high score updated.
     * @param highScore The new high score.
     * @return A new GameUiState with the updated high score.
     */
    fun withHighScore(highScore: Int): GameUiState {
        return copy(highScore = highScore)
    }
}
