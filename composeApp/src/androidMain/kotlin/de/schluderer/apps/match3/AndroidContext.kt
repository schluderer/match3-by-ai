package de.schluderer.apps.match3

import android.app.Application
import android.content.Context

/**
 * Holds the application context for Android.
 */
object AndroidContext {
    private var applicationContext: Context? = null

    /**
     * Initializes the application context.
     * This should be called from the Application class.
     * @param context The application context.
     */
    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    /**
     * Gets the application context.
     * @return The application context.
     * @throws IllegalStateException if the context has not been initialized.
     */
    fun get(): Context {
        return applicationContext ?: throw IllegalStateException(
            "AndroidContext not initialized. Call AndroidContext.initialize() in your Application class."
        )
    }
}

/**
 * Gets the application context.
 * @return The application context.
 */
fun getApplicationContext(): Context {
    return AndroidContext.get()
}