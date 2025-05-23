package de.schluderer.apps.match3

import android.app.Application

/**
 * Custom Application class for the Match-3 game.
 * Initializes the AndroidContext.
 */
class Match3Application : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidContext.initialize(this)
    }
}