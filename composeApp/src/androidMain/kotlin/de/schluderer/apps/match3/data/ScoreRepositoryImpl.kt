package de.schluderer.apps.match3.data

import android.content.Context
import android.content.SharedPreferences
import de.schluderer.apps.match3.getApplicationContext

/**
 * Android implementation of ScoreRepository using SharedPreferences.
 */
class ScoreRepositoryImpl(private val context: Context) : ScoreRepository {
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val PREFS_NAME = "match3_prefs"
        private const val KEY_HIGH_SCORE = "high_score"
    }

    override suspend fun getHighScore(): Int {
        return sharedPreferences.getInt(KEY_HIGH_SCORE, 0)
    }

    override suspend fun saveHighScore(score: Int): Boolean {
        val currentHighScore = getHighScore()
        if (score > currentHighScore) {
            sharedPreferences.edit().putInt(KEY_HIGH_SCORE, score).apply()
            return true
        }
        return false
    }
}

/**
 * Creates an Android-specific implementation of ScoreRepository.
 */
actual fun createScoreRepository(): ScoreRepository {
    return ScoreRepositoryImpl(getApplicationContext())
}