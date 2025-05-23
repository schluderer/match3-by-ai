package de.schluderer.apps.match3.data

/**
 * Repository for managing high scores.
 */
interface ScoreRepository {
    /**
     * Gets the current high score.
     * @return The high score, or 0 if no high score exists.
     */
    suspend fun getHighScore(): Int
    
    /**
     * Saves a new high score if it's higher than the current one.
     * @param score The score to save.
     * @return True if the score was saved as a new high score, false otherwise.
     */
    suspend fun saveHighScore(score: Int): Boolean
}

/**
 * Creates a platform-specific implementation of ScoreRepository.
 * @return A ScoreRepository implementation.
 */
expect fun createScoreRepository(): ScoreRepository