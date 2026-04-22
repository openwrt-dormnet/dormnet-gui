package io.github.sgpublic.dormnet.targets.template

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.sgpublic.dormnet.targets.Res
import io.github.sgpublic.dormnet.targets.core.DormnetTarget
import io.github.sgpublic.dormnet.targets.core.DormnetViewModel
import io.github.sgpublic.dormnet.targets.core.LoginParams
import io.github.sgpublic.dormnet.targets.login_device
import io.github.sgpublic.dormnet.targets.login_device_mobile
import io.github.sgpublic.dormnet.targets.login_device_pc
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference

enum class DormnetDevice {
    PC,
    Mobile,
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

        Card {
            OverlayDropdownPreference(
                title = stringResource(Res.string.login_device),
                items = DormnetDevice.entries.map {
                    stringResource(it.label)
                },
                selectedIndex = viewModel.device.ordinal,
                onSelectedIndexChange = {
                    viewModel.device = DormnetDevice.entries[it]
                },
            )
        }
    }

    protected val DormnetDevice.label: StringResource get() = when (this) {
        DormnetDevice.PC -> Res.string.login_device_pc
        DormnetDevice.Mobile -> Res.string.login_device_mobile
    }

    override fun createViewModel(): UserPwdDeviceModel {
        return UserPwdDeviceModel()
    }
}
