package io.github.sgpublic.dormnet.targets.template

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.sgpublic.dormnet.targets.core.DormnetTarget
import io.github.sgpublic.dormnet.targets.core.DormnetViewModel
import io.github.sgpublic.dormnet.targets.core.LoginParams

enum class DormnetDevice {
    PC, Mobile
}

expect val DefaultDormnetDevice: DormnetDevice

interface UserPwdDeviceTargetParams: UserPwdTargetParams {
    val device: DormnetDevice
}

open class UserPwdDeviceTargetParamsData(
    override val username: String,
    override val password: String,
    override val device: DormnetDevice,
): UserPwdDeviceTargetParams, LoginParams


open class UserPwdDeviceModel: DormnetViewModel<UserPwdDeviceTargetParamsData>(), UserPwdDeviceTargetParams {
    override var username by mutableStateOf("")
    var usernameMessage by mutableStateOf<String?>(null)
    override var password by mutableStateOf("")
    var passwordMessage by mutableStateOf<String?>(null)
    override var device by mutableStateOf(DefaultDormnetDevice)

    override fun createLoginParams(): UserPwdDeviceTargetParamsData {
        return UserPwdDeviceTargetParamsData(
            username = username,
            password = password,
            device = device,
        )
    }
}

abstract class UserPwdDeviceTarget: DormnetTarget<UserPwdDeviceTargetParamsData>() {
    @Composable
    override fun invoke() {
        val viewModel = viewModel<UserPwdDeviceModel>()
        UserPwdTarget.Ui(
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
        )

    }

    override fun createViewModel(): UserPwdDeviceModel {
        return UserPwdDeviceModel()
    }
}
