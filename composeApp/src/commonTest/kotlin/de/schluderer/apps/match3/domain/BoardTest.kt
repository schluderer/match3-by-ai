package de.schluderer.apps.match3.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull

class BoardTest {

    @Test
    fun testCreateRandom() {
        // Given
        val width = 5
        val height = 6

        // When
        val board = Board.createRandom(width, height)

        // Then
        assertEquals(width, board.width)
        assertEquals(height, board.height)
        assertEquals(width * height, board.tiles.size)

        // Check that all positions are filled
        for (y in 0 until height) {
            for (x in 0 until width) {
                val position = Position(x, y)
                val tile = board.getTileAt(position)
                assertNotNull(tile)
                assertEquals(position, tile.position)
            }
        }
    }

    @Test
    fun testGetTileAt() {
        // Given
        val board = createTestBoard(3, 3)

        // When & Then
        val tile = board.getTileAt(Position(1, 1))
        assertNotNull(tile)
        assertEquals(Position(1, 1), tile.position)
        assertEquals(TileColor.RED, tile.color)

        // Test getting a tile at an invalid position
        val invalidTile = board.getTileAt(Position(5, 5))
        assertNull(invalidTile)
    }

    @Test
    fun testIsValidPosition() {
        // Given
        val board = createTestBoard(3, 3)

        // When & Then
        assertTrue(board.isValidPosition(Position(0, 0)))
        assertTrue(board.isValidPosition(Position(2, 2)))
        assertFalse(board.isValidPosition(Position(-1, 0)))
        assertFalse(board.isValidPosition(Position(0, -1)))
        assertFalse(board.isValidPosition(Position(3, 0)))
        assertFalse(board.isValidPosition(Position(0, 3)))
    }

    @Test
    fun testSwapTiles() {
        // Given
        val board = createTestBoard(3, 3)
        val position1 = Position(0, 0)
        val position2 = Position(1, 0)

        // When
        val newBoard = board.swapTiles(position1, position2)

        // Then
        val tile1 = newBoard.getTileAt(position1)
        val tile2 = newBoard.getTileAt(position2)
        assertNotNull(tile1)
        assertNotNull(tile2)
        assertEquals(TileColor.GREEN, tile1.color)
        assertEquals(TileColor.RED, tile2.color)

        // Test swapping with invalid position
        val invalidBoard = board.swapTiles(position1, Position(5, 5))
        assertEquals(board, invalidBoard)

        // Test swapping non-adjacent tiles
        val nonAdjacentBoard = board.swapTiles(Position(0, 0), Position(2, 2))
        assertEquals(board, nonAdjacentBoard)
    }

    @Test
    fun testFindMatches() {
        // Given
        val tiles = listOf(
            Tile(Tile.nextId(), TileColor.RED, Position(0, 0)),
            Tile(Tile.nextId(), TileColor.RED, Position(1, 0)),
            Tile(Tile.nextId(), TileColor.RED, Position(2, 0)),
            Tile(Tile.nextId(), TileColor.BLUE, Position(0, 1)),
            Tile(Tile.nextId(), TileColor.BLUE, Position(1, 1)),
            Tile(Tile.nextId(), TileColor.GREEN, Position(2, 1)),
            Tile(Tile.nextId(), TileColor.YELLOW, Position(0, 2)),
            Tile(Tile.nextId(), TileColor.YELLOW, Position(1, 2)),
            Tile(Tile.nextId(), TileColor.YELLOW, Position(2, 2))
        )
        val board = Board(3, 3, tiles)

        // When
        val matches = board.findMatches()

        // Then
        assertEquals(6, matches.size)
        assertTrue(matches.contains(Position(0, 0)))
        assertTrue(matches.contains(Position(1, 0)))
        assertTrue(matches.contains(Position(2, 0)))
        assertTrue(matches.contains(Position(0, 2)))
        assertTrue(matches.contains(Position(1, 2)))
        assertTrue(matches.contains(Position(2, 2)))
    }

    @Test
    fun testRemoveTiles() {
        // Given
        val board = createTestBoard(3, 3)
        val positionsToRemove = listOf(Position(0, 0), Position(1, 1))

        // When
        val newBoard = board.removeTiles(positionsToRemove)

        // Then
        assertEquals(7, newBoard.tiles.size)
        assertNull(newBoard.getTileAt(Position(0, 0)))
        assertNull(newBoard.getTileAt(Position(1, 1)))
        assertNotNull(newBoard.getTileAt(Position(2, 2)))
    }

    @Test
    fun testApplyGravity() {
        // Given
        val tiles = listOf(
            Tile(Tile.nextId(), TileColor.RED, Position(0, 0)),
            // Gap at (0, 1)
            Tile(Tile.nextId(), TileColor.BLUE, Position(0, 2)),
            Tile(Tile.nextId(), TileColor.GREEN, Position(1, 0)),
            Tile(Tile.nextId(), TileColor.YELLOW, Position(1, 1)),
            Tile(Tile.nextId(), TileColor.PURPLE, Position(1, 2))
        )
        val board = Board(2, 3, tiles)

        // When
        val newBoard = board.applyGravity()

        // Then
        assertEquals(5, newBoard.tiles.size)

        // Check that tiles have fallen correctly
        val tile00 = newBoard.getTileAt(Position(0, 1))
        assertNotNull(tile00)
        assertEquals(TileColor.RED, tile00.color)

        val tile02 = newBoard.getTileAt(Position(0, 2))
        assertNotNull(tile02)
        assertEquals(TileColor.BLUE, tile02.color)

        // Column 1 should remain unchanged
        val tile10 = newBoard.getTileAt(Position(1, 0))
        assertNotNull(tile10)
        assertEquals(TileColor.GREEN, tile10.color)

        val tile11 = newBoard.getTileAt(Position(1, 1))
        assertNotNull(tile11)
        assertEquals(TileColor.YELLOW, tile11.color)

        val tile12 = newBoard.getTileAt(Position(1, 2))
        assertNotNull(tile12)
        assertEquals(TileColor.PURPLE, tile12.color)
    }

    @Test
    fun testFillEmptySpaces() {
        // Given
        val tiles = listOf(
            Tile(Tile.nextId(), TileColor.RED, Position(0, 0)),
            // Gap at (0, 1)
            Tile(Tile.nextId(), TileColor.BLUE, Position(0, 2)),
            Tile(Tile.nextId(), TileColor.GREEN, Position(1, 0)),
            Tile(Tile.nextId(), TileColor.YELLOW, Position(1, 1)),
            Tile(Tile.nextId(), TileColor.PURPLE, Position(1, 2))
        )
        val board = Board(2, 3, tiles)

        // When
        val newBoard = board.fillEmptySpaces()

        // Then
        assertEquals(6, newBoard.tiles.size)
        assertNotNull(newBoard.getTileAt(Position(0, 1)))
    }

    @Test
    fun testHasValidMoves() {
        // Given
        // Create a board with valid moves
        val tiles1 = listOf(
            Tile(Tile.nextId(), TileColor.RED, Position(0, 0)),
            Tile(Tile.nextId(), TileColor.RED, Position(1, 0)),
            Tile(Tile.nextId(), TileColor.GREEN, Position(2, 0)),
            Tile(Tile.nextId(), TileColor.BLUE, Position(0, 1)),
            Tile(Tile.nextId(), TileColor.YELLOW, Position(1, 1)),
            Tile(Tile.nextId(), TileColor.RED, Position(2, 1))
        )
        val boardWithValidMoves = Board(3, 2, tiles1)

        // Create a board with no valid moves
        val tiles2 = listOf(
            Tile(Tile.nextId(), TileColor.RED, Position(0, 0)),
            Tile(Tile.nextId(), TileColor.GREEN, Position(1, 0)),
            Tile(Tile.nextId(), TileColor.BLUE, Position(0, 1)),
            Tile(Tile.nextId(), TileColor.YELLOW, Position(1, 1))
        )
        val boardWithNoValidMoves = Board(2, 2, tiles2)

        // When & Then
        assertTrue(boardWithValidMoves.hasValidMoves())
        assertFalse(boardWithNoValidMoves.hasValidMoves())
    }

    // Helper method to create a test board with predictable tiles
    private fun createTestBoard(width: Int, height: Int): Board {
        val tiles = mutableListOf<Tile>()

        for (y in 0 until height) {
            for (x in 0 until width) {
                val position = Position(x, y)
                val color = when {
                    x == 0 && y == 0 -> TileColor.RED
                    x == 1 && y == 0 -> TileColor.GREEN
                    x == 0 && y == 1 -> TileColor.BLUE
                    x == 1 && y == 1 -> TileColor.RED
                    else -> TileColor.YELLOW
                }
                tiles.add(Tile(Tile.nextId(), color, position))
            }
        }

        return Board(width, height, tiles)
    }
}
