package io.github.sgpublic.dormnet

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.Dimension

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "DormNet",
    ) {
        val density = LocalDensity.current
        window.minimumSize = with(density) {
            Dimension(360.dp.toPx().toInt(), 560.dp.toPx().toInt())
        }
        App()
    }
}