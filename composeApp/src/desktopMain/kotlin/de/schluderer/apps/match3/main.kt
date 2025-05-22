package de.schluderer.apps.match3

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "match3",
    ) {
        App()
    }
}