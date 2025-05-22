package de.schluderer.apps.match3

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform