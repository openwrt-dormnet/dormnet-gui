package io.github.sgpublic.dormnet.targets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.sgpublic.dormnet.core.Config
import io.github.sgpublic.dormnet.core.encodeBase64
import io.github.sgpublic.dormnet.core.parse
import io.github.sgpublic.dormnet.targets.core.DormnetTargetEntry
import io.github.sgpublic.dormnet.targets.template.DormnetDevice
import io.github.sgpublic.dormnet.targets.template.UserPwdDeviceModel
import io.github.sgpublic.dormnet.targets.template.UserPwdDeviceTarget
import io.github.sgpublic.dormnet.targets.template.UserPwdDeviceTargetParamsData
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

@DormnetTargetEntry(
    maintainer = ["sgpublic"]
)
object CQCAI : UserPwdDeviceTarget() {
    override val title: StringResource = Res.string.school_cqcai
    override val HttpClient: HttpClient = super.HttpClient.config {
        followRedirects = false
    }

    @Composable
    override fun invoke(loading: Boolean, onLoadingChanged: (Boolean) -> Unit) {
        super.invoke(loading, onLoadingChanged)
        val viewModel = viewModel<UserPwdDeviceModel>()
        LaunchedEffect(Unit) {
            viewModel.username = Config[CqcaiUsernameKey] ?: ""
            viewModel.password = Config[CqcaiPasswordKey] ?: ""
            Config[CqcaiDeviceKey].parse<DormnetDevice> {
                viewModel.device = it
            }
        }
    }

    override suspend fun doLogin(params: UserPwdDeviceTargetParamsData): Result<String> {
        Config {
            it[CqcaiUsernameKey] = params.username
            it[CqcaiPasswordKey] = params.password
            it[CqcaiDeviceKey] = params.device.name
        }
        return runCatching {
            val parameters = requestLoginParameters().getOrThrow()
            val response = requestLogin(
                userInfo = params,
                netParams = parameters,
            )
            val message = response.message ?: getString(Res.string.login_failed)
            check(response.result == "1") { message }
            message
        }
    }

    private suspend fun failedResult(key: String): Result<CqcaiNetworkInfo> {
        return Result.failure(IllegalStateException(getString(
            Res.string.school_cqcai_failed_redirect_info, key
        )))
    }
    private suspend fun requestLoginParameters(): Result<CqcaiNetworkInfo> {
        val response = HttpClient.get("http://192.168.198.1")

        val redirectUrl = response.headers[HttpHeaders.Location] ?: return failedResult("Location")
        val params = Url(redirectUrl).parameters
        val wlanuserip = params["wlanuserip"] ?: return failedResult("wlanuserip")
        val wlanacname = params["wlanacname"] ?: return failedResult("wlanacname")
        val wlanacip = params["wlanacip"] ?: return failedResult("wlanacip")
        val mac = params["mac"] ?: return failedResult("mac")
        return Result.success(CqcaiNetworkInfo(
            wlanuserip = wlanuserip,
            wlanacname = wlanacname,
            wlanacip = wlanacip,
            mac = mac,
        ))
    }

    private suspend fun requestLogin(
        userInfo: UserPwdDeviceTargetParamsData,
        netParams: CqcaiNetworkInfo,
    ): CqcaiEportalResponse {

        val response = HttpClient.get("https://auth.cqcai.edu.cn:802/eportal/portal/login") {
            parameter("c", "Portal")
            parameter("a", "login")
            parameter("callback", userInfo.device.callback)
            parameter("login_method", "1")
            parameter("is_base64encode", "1")
            parameter(
                "user_account",
                ",${userInfo.device.accountPrefix},${userInfo.username}".encodeBase64(),
            )
            parameter("user_password", userInfo.password.encodeBase64())
            parameter("wlan_user_ip", netParams.wlanuserip)
            parameter("wlan_user_ipv6", "")
            parameter("wlan_user_mac", netParams.mac.replace(":", "").lowercase())
            parameter("wlan_ac_ip", netParams.wlanacip)
            parameter("wlan_ac_name", netParams.wlanacname)
            parameter("jsVersion", "4.2.1")
            parameter("terminal_type", userInfo.device.terminalType)
            header(HttpHeaders.UserAgent, userInfo.device.userAgent)
            header(HttpHeaders.Referrer, "https://auth.cqcai.edu.cn/")
            header("DNT", "1")
        }

        val body = response.bodyAsText()
        check(body.length > 2) { "Empty response of login request." }
        val json = body.substringAfter("(", missingDelimiterValue = body)
            .substringBeforeLast(")")
        val obj = Json.parseToJsonElement(json).jsonObject
        return CqcaiEportalResponse(
            result = obj["result"]?.jsonPrimitive?.content,
            message = obj["msg"]?.jsonPrimitive?.content,
        )
    }
}

private data class CqcaiEportalResponse(
    val result: String?,
    val message: String?,
)

private data class CqcaiNetworkInfo(
    val wlanuserip: String,
    val wlanacname: String,
    val wlanacip: String,
    val mac: String,
)

private val DormnetDevice.accountPrefix: String get() = when (this) {
    DormnetDevice.PC -> "0"
    DormnetDevice.Mobile -> "1"
}
private val DormnetDevice.terminalType: String get() = when (this) {
    DormnetDevice.PC -> "1"
    DormnetDevice.Mobile -> "2"
}

private val DormnetDevice.callback: String get() = when (this) {
    DormnetDevice.PC -> "dr1020"
    DormnetDevice.Mobile -> "dr1012"
}

private val DormnetDevice.userAgent: String get() = when (this) {
    DormnetDevice.PC -> "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 Edg/141.0.0.0"
    DormnetDevice.Mobile -> "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Mobile Safari/537.36 EdgA/141.0.0.0"
}

private val CqcaiUsernameKey = stringPreferencesKey("cqcai_username")
private val CqcaiPasswordKey = stringPreferencesKey("cqcai_password")
private val CqcaiDeviceKey = stringPreferencesKey("cqcai_device")
