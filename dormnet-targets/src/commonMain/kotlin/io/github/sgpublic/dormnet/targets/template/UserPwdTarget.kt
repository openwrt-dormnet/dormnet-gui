package io.github.sgpublic.dormnet.targets.template

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import io.github.sgpublic.dormnet.targets.Res
import io.github.sgpublic.dormnet.targets.core.DormnetTarget
import io.github.sgpublic.dormnet.targets.core.DormnetViewModel
import io.github.sgpublic.dormnet.targets.core.LoginParams
import io.github.sgpublic.dormnet.targets.login_password
import io.github.sgpublic.dormnet.targets.login_username
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

interface UserPwdTargetParams {
    val username: String
    val password: String
}

open class UserPwdTargetParamsData(
    override val username: String,
    override val password: String
): UserPwdTargetParams, LoginParams

open class UserPwdModel: DormnetViewModel<UserPwdTargetParamsData>(), UserPwdTargetParams {
    override var username by mutableStateOf("")
    var usernameMessage by mutableStateOf<String?>(null)
    override var password by mutableStateOf("")
    var passwordMessage by mutableStateOf<String?>(null)

    override fun createLoginParams(): UserPwdTargetParamsData {
        return UserPwdTargetParamsData(
            username = username,
            password = password,
        )
    }
}

abstract class UserPwdTarget: DormnetTarget<UserPwdTargetParamsData>() {
    @Composable
    override fun invoke(loading: Boolean, onLoadingChanged: (Boolean) -> Unit) {
        val viewModel = viewModel<UserPwdModel>()
        Ui(
            username = viewModel.username,
            usernameMessage = viewModel.usernameMessage,
            onUsernameChanged = {
                viewModel.username = it
            },
            password = viewModel.password,
            passwordMessage = viewModel.passwordMessage,
            onPasswordChanged = {
                viewModel.password = it
            },
            enabled = !loading
        )
    }

    override fun createViewModel(): UserPwdModel {
        return UserPwdModel()
    }

    companion object {
        @Composable
        fun Ui(
            username: String,
            usernameMessage: String?,
            onUsernameChanged: (String) -> Unit,
            password: String,
            passwordMessage: String?,
            onPasswordChanged: (String) -> Unit,
            enabled: Boolean,
        ) {
            TextField(
                label = stringResource(Res.string.login_username),
                value = username,
                onValueChange = onUsernameChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = enabled,
            )
            usernameMessage?.let {
                Text(
                    text = it,
                    color = MiuixTheme.colorScheme.error,
                    style = MiuixTheme.textStyles.body2,
                )
            }
            TextField(
                label = stringResource(Res.string.login_password),
                value = password,
                onValueChange = onPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                enabled = enabled,
            )
            passwordMessage?.let {
                Text(
                    text = it,
                    color = MiuixTheme.colorScheme.error,
                    style = MiuixTheme.textStyles.body2,
                )
            }
        }
    }
}
