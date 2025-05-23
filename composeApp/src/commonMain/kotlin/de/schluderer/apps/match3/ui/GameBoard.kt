package de.schluderer.apps.match3.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.schluderer.apps.match3.domain.Position
import de.schluderer.apps.match3.domain.Tile
import de.schluderer.apps.match3.presentation.GameUiState

/**
 * Data class to hold animation state for a tile.
 */
private data class AnimationState(
    val currentPosition: Pair<Int, Int>,
    val previousPosition: Pair<Int, Int>,
    val needsAnimation: Boolean
)

/**
 * Displays the game board with tiles.
 * @param uiState The current UI state of the game.
 * @param onTileTap Callback for when a tile is tapped.
 */
@Composable
fun GameBoard(
    uiState: GameUiState,
    onTileTap: (Int, Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Create a board with constraints to calculate tile sizes
        BoxWithConstraints(
            modifier = Modifier
                .aspectRatio(uiState.board.width.toFloat() / uiState.board.height.toFloat())
                .fillMaxSize(0.9f)
                .background(Color(0xFF333333)),
            contentAlignment = Alignment.TopStart
        ) {
            val boardWidth = maxWidth
            val boardHeight = maxHeight

            // Calculate tile size
            val tileWidth = boardWidth / uiState.board.width
            val tileHeight = boardHeight / uiState.board.height

            // Keep track of last known positions for animations
            val lastKnownPositions = remember { mutableStateOf(emptyMap<Int, Pair<Int, Int>>()) }

            // Derive lastKnownPositions from the current state instead of using LaunchedEffect
            // This eliminates the race condition with the other LaunchedEffect
            val derivedLastKnownPositions by remember(uiState.board, uiState.animatingTileIds, lastKnownPositions.value) {
                derivedStateOf {
                    println("[DEBUG_LOG] Deriving lastKnownPositions. Animating tile IDs: ${uiState.animatingTileIds}")

                    // Get current tile IDs on the board
                    val currentTileIds = uiState.tiles.map { it.id }.toSet()

                    // Start with current positions or existing map
                    val updatedMap = lastKnownPositions.value.filter {
                        // Remove positions for tiles that are no longer on the board
                        !currentTileIds.contains(it.key)
                    }.toMutableMap()

                    // Add or update positions for new tiles
                    uiState.tiles.forEach { tile ->
                        // Initialize new tiles
                        if (!updatedMap.containsKey(tile.id)) {
                            updatedMap[tile.id] = Pair(tile.position.x, tile.position.y)
                        }
                    }

                    println("[DEBUG_LOG] Derived lastKnownPositions: $updatedMap")
                    updatedMap
                }
            }

            // Derive animation states based on current and last known positions
            val animationStates by remember(uiState.board, uiState.animatingTileIds, derivedLastKnownPositions) {
                derivedStateOf {
                    val states = uiState.tiles.associate { tile ->
                        val tileId = tile.id
                        val currentPos = Pair(tile.position.x, tile.position.y)
                        val lastPos = derivedLastKnownPositions[tile.id] ?: currentPos
                        val positionChanged = currentPos != lastPos
                        val isAnimating = uiState.isAnimating(tile.id)

                        // Changed animation trigger condition: animate ONLY if position changed
                        val needsAnimation = positionChanged

                        // Debug logging for animation state
                        if (positionChanged) {
                            println("[DEBUG_LOG] Tile $tileId: currentPos=$currentPos, lastPos=$lastPos, positionChanged=$positionChanged, needsAnimation=$needsAnimation")
                        }

                        tileId to AnimationState(
                            currentPosition = currentPos,
                            previousPosition = lastPos,
                            needsAnimation = needsAnimation
                        )
                    }

                    // Log summary of animation states
                    val animatingTiles = states.filter { it.value.needsAnimation }
                    if (animatingTiles.isNotEmpty()) {
                        println("[DEBUG_LOG] Animating tiles: ${animatingTiles.keys}")
                    }

                    states
                }
            }

            // Draw all tiles
            uiState.tiles.forEach { tile ->
                val x = tile.position.x
                val y = tile.position.y
                val tileId = tile.id

                // Get the animation state for this tile
                val animState = animationStates[tileId] ?: AnimationState(
                    currentPosition = Pair(x, y),
                    previousPosition = Pair(x, y),
                    needsAnimation = false
                )

                // Extract previous position
                val (prevX, prevY) = animState.previousPosition

                // Determine if animation is needed
                val needsAnimation = animState.needsAnimation

                // Always use a reasonable animation duration to ensure smooth transitions
                // Even for non-animating tiles, a short duration helps prevent jarring jumps
                val animationDuration = when {
                    needsAnimation -> 300
                    else -> 150  // Use a shorter duration for non-animating tiles
                }

                // For x position: animate when needed
                val animatedX by animateDpAsState(
                    targetValue = tileWidth * x,
                    animationSpec = tween(durationMillis = animationDuration)
                )

                // For y position: animate when needed
                val animatedY by animateDpAsState(
                    targetValue = tileHeight * y,
                    animationSpec = tween(durationMillis = animationDuration)
                )

                // Use LaunchedEffect to update the position after animation completes
                // This is more reliable than using finishedListener which might not always be called
                LaunchedEffect(x, y, needsAnimation) {
                    if (needsAnimation) {
                        // Add a small delay to ensure animation has time to start
                        delay(animationDuration.toLong() + 50)

                        // Update both x and y positions at once
                        val updatedMap = lastKnownPositions.value.toMutableMap()
                        updatedMap[tileId] = Pair(x, y)
                        lastKnownPositions.value = updatedMap

                        println("[DEBUG_LOG] Updated lastKnownPositions for tile $tileId to ($x, $y)")
                    }
                }

                // Animate the tile scale when selected or animating
                val scale by animateFloatAsState(
                    targetValue = when {
                        needsAnimation -> 0.9f
                        uiState.isSelected(tile.position) -> 1.2f
                        else -> 1f
                    },
                    animationSpec = tween(durationMillis = 300)
                )

                // Position the tile based on its coordinates with animation
                Box(
                    modifier = Modifier
                        .size(tileWidth, tileHeight)
                        .offset(
                            x = animatedX,
                            y = animatedY
                        )
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(tile.color.color)
                        .border(
                            width = if (uiState.isSelected(tile.position)) 3.dp else 1.dp,
                            color = if (uiState.isSelected(tile.position)) 
                                MaterialTheme.colorScheme.primary 
                            else if (needsAnimation)
                                Color.Yellow  // Highlight animating tiles
                            else 
                                Color.White.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .scale(scale)
                        .clickable { onTileTap(x, y) },
                    contentAlignment = Alignment.Center
                ) {
                    // Add debug text to show tile ID and animation state
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Draw a small indicator in the corner for animating tiles
                        if (needsAnimation) {
                            drawCircle(
                                color = Color.Red,
                                radius = size.minDimension * 0.1f,
                                center = center.copy(
                                    x = center.x * 0.5f,
                                    y = center.y * 0.5f
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
