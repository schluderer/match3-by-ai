package de.schluderer.apps.match3.data

import java.io.File
import java.util.Properties

/**
 * Desktop implementation of ScoreRepository using a properties file.
 */
class ScoreRepositoryImpl : ScoreRepository {
    private val propertiesFile = File(System.getProperty("user.home"), ".match3/scores.properties")
    private val properties = Properties()

    companion object {
        private const val KEY_HIGH_SCORE = "high_score"
    }

    init {
        // Create directory if it doesn't exist
        propertiesFile.parentFile?.mkdirs()
        
        // Load properties if file exists
        if (propertiesFile.exists()) {
            propertiesFile.inputStream().use {
                properties.load(it)
            }
        }
    }

    override suspend fun getHighScore(): Int {
        return properties.getProperty(KEY_HIGH_SCORE, "0").toInt()
    }

    override suspend fun saveHighScore(score: Int): Boolean {
        val currentHighScore = getHighScore()
        if (score > currentHighScore) {
            properties.setProperty(KEY_HIGH_SCORE, score.toString())
            propertiesFile.outputStream().use {
                properties.store(it, "Match-3 High Scores")
            }
            return true
        }
        return false
    }
}

/**
 * Creates a Desktop-specific implementation of ScoreRepository.
 */
actual fun createScoreRepository(): ScoreRepository {
    return ScoreRepositoryImpl()
}