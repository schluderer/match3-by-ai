package de.schluderer.apps.match3.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import de.schluderer.apps.match3.presentation.Match3ViewModel

/**
 * The main composable for the Match-3 game.
 */
@Composable
fun Match3App() {
    // Create the view model
    val viewModel = remember { Match3ViewModel() }
    
    // Collect the UI state
    val uiState by viewModel.uiState.collectAsState()
    
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Score bar
                ScoreBar(
                    score = uiState.score,
                    highScore = uiState.highScore,
                    gameOver = uiState.gameOver,
                    onNewGameClick = { viewModel.newGame() }
                )
                
                // Game board
                GameBoard(
                    uiState = uiState,
                    onTileTap = { x, y -> viewModel.onTileTapped(x, y) }
                )
            }
        }
    }
}