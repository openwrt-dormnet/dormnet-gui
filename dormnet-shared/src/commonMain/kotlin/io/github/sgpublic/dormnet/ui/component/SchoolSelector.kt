package io.github.sgpublic.dormnet.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.unit.dp
import io.github.sgpublic.dormnet.generated.resources.Res
import io.github.sgpublic.dormnet.generated.resources.school_empty
import io.github.sgpublic.dormnet.generated.resources.school_label
import io.github.sgpublic.dormnet.generated.resources.school_placeholder
import io.github.sgpublic.dormnet.targets.core.DormnetTarget
import io.github.sgpublic.dormnet.targets.core.DormnetTargetRegistry
import io.github.sgpublic.dormnet.targets.core.LoginParams
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.theme.MiuixTheme


@Composable
fun SchoolSelector(
    modifier: Modifier = Modifier,
    targets: List<DormnetTarget<out LoginParams>> = DormnetTargetRegistry.all,
    onSelected: (DormnetTarget<out LoginParams>?) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val schoolLabel = stringResource(Res.string.school_label)
    val schoolPlaceholder = stringResource(Res.string.school_placeholder)
    val schools = targets.associateWith { target ->
        stringResource(target.title)
    }
    val filteredSchools = remember(query, schools) {
        schools.filter { (_, title) ->
            title.contains(query, ignoreCase = true)
        }
    }

    Box(
        modifier = modifier,
    ) {
        TextField(
            value = query,
            onValueChange = {
                query = it
                expanded = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusEvent {
                    if (it.isFocused) {
                        expanded = true
                    }
                },
            singleLine = true,
            label = if (query.isEmpty()) schoolPlaceholder else schoolLabel,
            useLabelAsPlaceholder = true,
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            query = ""
                            onSelected(null)
                        },
                        modifier = Modifier.focusProperties {
                            canFocus = false
                        },
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            },
        )

        if (expanded) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 64.dp)
                    .fillMaxWidth(),
                insideMargin = PaddingValues(vertical = 8.dp),
            ) {
                if (filteredSchools.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.school_empty),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        style = MiuixTheme.textStyles.body2,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(160.dp),
                    ) {
                        items(
                            filteredSchools.entries.toList()
                        ) { (school, title) ->
                            Text(
                                text = title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        query = title
                                        expanded = false
                                        onSelected(school)
                                    }
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
