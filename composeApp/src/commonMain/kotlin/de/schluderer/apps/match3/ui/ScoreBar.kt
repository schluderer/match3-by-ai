package de.schluderer.apps.match3.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Displays the score bar with current score, high score, and game controls.
 * @param score The current score.
 * @param highScore The high score.
 * @param gameOver Whether the game is over.
 * @param onNewGameClick Callback for when the new game button is clicked.
 */
@Composable
fun ScoreBar(
    score: Int,
    highScore: Int,
    gameOver: Boolean,
    onNewGameClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "Match-3",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Score and high score
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScoreDisplay(
                label = "Score",
                score = score
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            ScoreDisplay(
                label = "High Score",
                score = highScore
            )
        }
        
        // Game over message
        if (gameOver) {
            Text(
                text = "Game Over!",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        // New game button
        Button(
            onClick = onNewGameClick,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(text = if (gameOver) "New Game" else "Restart")
        }
    }
}

/**
 * Displays a score with a label.
 * @param label The label for the score.
 * @param score The score to display.
 */
@Composable
private fun ScoreDisplay(
    label: String,
    score: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        
        AnimatedContent(
            targetState = score,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            }
        ) { targetScore ->
            Text(
                text = "$targetScore",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        }
    }
}