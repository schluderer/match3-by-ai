package de.schluderer.apps.match3.data

import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of ScoreRepository using NSUserDefaults.
 */
class ScoreRepositoryImpl : ScoreRepository {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    companion object {
        private const val KEY_HIGH_SCORE = "high_score"
    }

    override suspend fun getHighScore(): Int {
        return userDefaults.integerForKey(KEY_HIGH_SCORE).toInt()
    }

    override suspend fun saveHighScore(score: Int): Boolean {
        val currentHighScore = getHighScore()
        if (score > currentHighScore) {
            userDefaults.setInteger(score.toLong(), KEY_HIGH_SCORE)
            return true
        }
        return false
    }
}

/**
 * Creates an iOS-specific implementation of ScoreRepository.
 */
actual fun createScoreRepository(): ScoreRepository {
    return ScoreRepositoryImpl()
}