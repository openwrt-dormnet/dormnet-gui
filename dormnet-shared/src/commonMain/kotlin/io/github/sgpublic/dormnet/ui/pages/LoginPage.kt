package io.github.sgpublic.dormnet.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowSizeClass
import io.github.sgpublic.dormnet.generated.resources.Res
import io.github.sgpublic.dormnet.generated.resources.app_name
import io.github.sgpublic.dormnet.generated.resources.login_action
import io.github.sgpublic.dormnet.generated.resources.login_subtitle
import io.github.sgpublic.dormnet.generated.resources.login_title
import io.github.sgpublic.dormnet.targets.core.DormnetTarget
import io.github.sgpublic.dormnet.targets.core.LocalDormnetViewModel
import io.github.sgpublic.dormnet.ui.component.SchoolSelector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme


@Composable
fun LoginPage() {
    val windowSizeClass = currentWindowAdaptiveInfo(
        supportLargeAndXLargeWidth = true,
    ).windowSizeClass
    val contentMaxWidth = when {
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> 600.dp
        else -> Dp.Unspecified
    }

    val scope = rememberCoroutineScope()

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .then(
                        if (contentMaxWidth != Dp.Unspecified) {
                            Modifier.widthIn(max = contentMaxWidth)
                        } else {
                            Modifier
                        }
                    )
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                var school: DormnetTarget<*>? by remember { mutableStateOf(null) }

                Text(
                    text = stringResource(Res.string.app_name),
                    style = MiuixTheme.textStyles.title1,
                )
                SchoolSelector(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    school = it
                }
                if (school == null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        insideMargin = PaddingValues(20.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.login_title),
                            style = MiuixTheme.textStyles.title3,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(Res.string.login_subtitle),
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                } else school?.let { school ->
                    val viewModel = viewModel { school.createViewModel() }
                    CompositionLocalProvider(
                        LocalDormnetViewModel provides viewModel
                    ) {
                        school()

                        Button(
                            onClick = {
                                viewModel.loading = true
                                val params = viewModel.createLoginParams()
                                scope.launch(Dispatchers.IO) {
                                    val result = school.login(params)
                                    viewModel.loading = false
                                }
                            },
                            modifier = Modifier.widthIn(320.dp)
                                .align(Alignment.CenterHorizontally),
                        ) {
                            Text(
                                text = stringResource(Res.string.login_action)
                            )
                        }
                    }
                }
            }
        }
    }
}
