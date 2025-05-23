package de.schluderer.apps.match3.domain

/**
 * Represents a tile on the game board.
 * @property id The unique identifier of the tile.
 * @property color The color of the tile.
 * @property position The position of the tile on the board.
 */
data class Tile(
    val id: Int,
    val color: TileColor,
    val position: Position
) {
    companion object {
        private var nextId = 1

        /**
         * Gets the next unique ID for a tile.
         * @return A unique ID.
         */
        fun nextId(): Int {
            return nextId++
        }
    }

    /**
     * Creates a copy of this tile with a new position.
     * @param newPosition The new position for the tile.
     * @return A new Tile with the same color and ID but at the new position.
     */
    fun moveTo(newPosition: Position): Tile {
        return copy(position = newPosition)
    }
}
