package io.github.sgpublic.dormnet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import io.github.sgpublic.dormnet.ui.pages.LoginPage
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

@Composable
@Preview
fun App() {
    val controller = remember {
        ThemeController(
            colorSchemeMode = ColorSchemeMode.MonetSystem,
            keyColor = Color(0xFF3482FF),
        )
    }
    MiuixTheme(
        controller = controller,
    ) {
        LoginPage()
    }
}
