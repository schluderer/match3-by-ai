package de.schluderer.apps.match3.domain

import kotlin.math.abs

/**
 * Represents a position on the game board.
 * @property x The x-coordinate (column) of the position.
 * @property y The y-coordinate (row) of the position.
 */
data class Position(val x: Int, val y: Int) {
    /**
     * Checks if this position is adjacent to another position.
     * Positions are adjacent if they are horizontally or vertically next to each other.
     * @param other The position to check adjacency with.
     * @return True if the positions are adjacent, false otherwise.
     */
    fun isAdjacentTo(other: Position): Boolean {
        val dx = abs(x - other.x)
        val dy = abs(y - other.y)
        // Adjacent if exactly one coordinate differs by 1 and the other is the same
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1)
    }
}
