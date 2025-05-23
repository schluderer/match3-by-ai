package de.schluderer.apps.match3.domain

import androidx.compose.ui.graphics.Color

/**
 * Represents the different colors of tiles in the game.
 * Each color has an associated Compose UI Color for rendering.
 */
enum class TileColor(val color: Color) {
    RED(Color(0xFFE57373)),
    GREEN(Color(0xFF81C784)),
    BLUE(Color(0xFF64B5F6)),
    YELLOW(Color(0xFFFFD54F)),
    PURPLE(Color(0xFFBA68C8)),
    ORANGE(Color(0xFFFFB74D));

    companion object {
        /**
         * Returns a random tile color.
         * @return A randomly selected tile color.
         */
        fun random(): TileColor {
            return values().random()
        }
    }
}