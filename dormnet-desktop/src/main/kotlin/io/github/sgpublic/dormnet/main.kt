package io.github.sgpublic.dormnet

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.sgpublic.dormnet.generated.resources.Res
import io.github.sgpublic.dormnet.generated.resources.ic_launcher
import org.jetbrains.compose.resources.painterResource
import java.awt.Dimension

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "DormNet",
        icon = painterResource(Res.drawable.ic_launcher),
    ) {
        val density = LocalDensity.current
        window.minimumSize = with(density) {
            Dimension(360.dp.toPx().toInt(), 560.dp.toPx().toInt())
        }
        App()
    }
}