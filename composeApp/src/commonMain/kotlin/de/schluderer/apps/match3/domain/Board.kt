package de.schluderer.apps.match3.domain

/**
 * Represents the game board.
 * @property width The width of the board (number of columns).
 * @property height The height of the board (number of rows).
 * @property tiles The tiles currently on the board.
 */
class Board(
    val width: Int,
    val height: Int,
    val tiles: List<Tile>
) {
    companion object {
        /**
         * The minimum number of tiles in a row or column to form a match.
         */
        const val MIN_MATCH = 3

        /**
         * Creates a new board with random tiles.
         * @param width The width of the board.
         * @param height The height of the board.
         * @return A new board with random tiles.
         */
        fun createRandom(width: Int, height: Int): Board {
            val tiles = mutableListOf<Tile>()

            for (y in 0 until height) {
                for (x in 0 until width) {
                    val position = Position(x, y)
                    val color = TileColor.random()
                    tiles.add(Tile(Tile.nextId(), color, position))
                }
            }

            return Board(width, height, tiles)
        }
    }

    /**
     * Gets the tile at the specified position.
     * @param position The position to get the tile from.
     * @return The tile at the position, or null if no tile exists at that position.
     */
    fun getTileAt(position: Position): Tile? {
        return tiles.find { it.position == position }
    }

    /**
     * Finds a tile by its unique ID.
     * @param id The ID of the tile to find.
     * @return The tile with the specified ID, or null if no tile with that ID exists.
     */
    fun findTileById(id: Int): Tile? {
        return tiles.find { it.id == id }
    }

    /**
     * Checks if the specified position is within the board boundaries.
     * @param position The position to check.
     * @return True if the position is within the board, false otherwise.
     */
    fun isValidPosition(position: Position): Boolean {
        return position.x in 0 until width && position.y in 0 until height
    }

    /**
     * Swaps two tiles on the board.
     * @param position1 The position of the first tile.
     * @param position2 The position of the second tile.
     * @return A new board with the tiles swapped.
     */
    fun swapTiles(position1: Position, position2: Position): Board {
        if (!isValidPosition(position1) || !isValidPosition(position2)) {
            return this
        }

        if (!position1.isAdjacentTo(position2)) {
            return this
        }

        val tile1 = getTileAt(position1) ?: return this
        val tile2 = getTileAt(position2) ?: return this

        val newTiles = tiles.toMutableList()
        val index1 = newTiles.indexOf(tile1)
        val index2 = newTiles.indexOf(tile2)

        newTiles[index1] = tile1.moveTo(position2)
        newTiles[index2] = tile2.moveTo(position1)

        return Board(width, height, newTiles)
    }

    /**
     * Swaps two tiles on the board by their IDs.
     * @param id1 The ID of the first tile.
     * @param id2 The ID of the second tile.
     * @return A new board with the tiles swapped, or this board if the swap is invalid.
     */
    fun swapTilesById(id1: Int, id2: Int): Board {
        val tile1 = findTileById(id1) ?: return this
        val tile2 = findTileById(id2) ?: return this

        // Check if positions are valid and adjacent
        if (!isValidPosition(tile1.position) || !isValidPosition(tile2.position)) {
            return this
        }

        if (!tile1.position.isAdjacentTo(tile2.position)) {
            return this
        }

        val newTiles = tiles.toMutableList()
        val index1 = newTiles.indexOf(tile1)
        val index2 = newTiles.indexOf(tile2)

        newTiles[index1] = tile1.moveTo(tile2.position)
        newTiles[index2] = tile2.moveTo(tile1.position)

        return Board(width, height, newTiles)
    }

    /**
     * Finds all matches on the board.
     * @return A list of positions forming matches.
     */
    fun findMatches(): List<Position> {
        val matches = mutableSetOf<Position>()

        // Check horizontal matches
        for (y in 0 until height) {
            var currentColor: TileColor? = null
            var matchLength = 1

            for (x in 0 until width) {
                val position = Position(x, y)
                val tile = getTileAt(position)

                if (tile != null && tile.color == currentColor) {
                    matchLength++
                } else {
                    // Check if we found a match before resetting
                    if (matchLength >= MIN_MATCH) {
                        for (i in 1..matchLength) {
                            matches.add(Position(x - i, y))
                        }
                    }

                    currentColor = tile?.color
                    matchLength = 1
                }
            }

            // Check for match at the end of the row
            if (matchLength >= MIN_MATCH) {
                for (i in 1..matchLength) {
                    matches.add(Position(width - i, y))
                }
            }
        }

        // Check vertical matches
        for (x in 0 until width) {
            var currentColor: TileColor? = null
            var matchLength = 1

            for (y in 0 until height) {
                val position = Position(x, y)
                val tile = getTileAt(position)

                if (tile != null && tile.color == currentColor) {
                    matchLength++
                } else {
                    // Check if we found a match before resetting
                    if (matchLength >= MIN_MATCH) {
                        for (i in 1..matchLength) {
                            matches.add(Position(x, y - i))
                        }
                    }

                    currentColor = tile?.color
                    matchLength = 1
                }
            }

            // Check for match at the end of the column
            if (matchLength >= MIN_MATCH) {
                for (i in 1..matchLength) {
                    matches.add(Position(x, height - i))
                }
            }
        }

        return matches.toList()
    }

    /**
     * Removes tiles at the specified positions and returns a new board.
     * @param positions The positions of tiles to remove.
     * @return A new board with the tiles removed.
     */
    fun removeTiles(positions: List<Position>): Board {
        val newTiles = tiles.filter { !positions.contains(it.position) }
        return Board(width, height, newTiles)
    }

    /**
     * Applies gravity to the board, making tiles fall to fill empty spaces.
     * @return A new board with tiles moved according to gravity.
     */
    fun applyGravity(): Board {
        val newTiles = mutableListOf<Tile>()

        // Process each column
        for (x in 0 until width) {
            val columnTiles = tiles.filter { it.position.x == x }
                .sortedBy { it.position.y }

            // Calculate new positions for tiles in this column
            var newY = height - 1
            for (tile in columnTiles.reversed()) {
                newTiles.add(tile.moveTo(Position(x, newY)))
                newY--
            }
        }

        return Board(width, height, newTiles)
    }

    /**
     * Fills empty spaces on the board with new random tiles.
     * @return A new board with new tiles added to fill empty spaces.
     */
    fun fillEmptySpaces(): Board {
        val newTiles = tiles.toMutableList()

        // Find all empty positions
        val occupiedPositions = tiles.map { it.position }
        val emptyPositions = mutableListOf<Position>()

        for (y in 0 until height) {
            for (x in 0 until width) {
                val position = Position(x, y)
                if (!occupiedPositions.contains(position)) {
                    emptyPositions.add(position)
                }
            }
        }

        // Add new tiles at empty positions
        for (position in emptyPositions) {
            newTiles.add(Tile(Tile.nextId(), TileColor.random(), position))
        }

        return Board(width, height, newTiles)
    }

    /**
     * Checks if there are any valid moves on the board.
     * @return True if there are valid moves, false otherwise.
     */
    fun hasValidMoves(): Boolean {
        // Check each position on the board
        for (y in 0 until height) {
            for (x in 0 until width) {
                val position = Position(x, y)

                // Try swapping with adjacent positions
                val adjacentPositions = listOf(
                    Position(x + 1, y),
                    Position(x, y + 1)
                ).filter { isValidPosition(it) }

                for (adjacentPosition in adjacentPositions) {
                    val swappedBoard = swapTiles(position, adjacentPosition)
                    if (swappedBoard.findMatches().isNotEmpty()) {
                        return true
                    }
                }
            }
        }

        return false
    }
}
