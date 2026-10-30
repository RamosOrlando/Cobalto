package com.template.cobalto

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.cobalto.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Cobalto",
    ) {
        App()
    }
}
