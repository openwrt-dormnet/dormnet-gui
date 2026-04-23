package io.github.sgpublic.dormnet.targets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.sgpublic.dormnet.core.Config
import io.github.sgpublic.dormnet.core.parse
import io.github.sgpublic.dormnet.targets.core.DormnetTargetEntry
import io.github.sgpublic.dormnet.targets.template.DormnetDevice
import io.github.sgpublic.dormnet.targets.template.UserPwdDeviceTarget
import io.github.sgpublic.dormnet.targets.template.UserPwdDeviceModel
import io.github.sgpublic.dormnet.targets.template.UserPwdDeviceTargetParamsData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference

class CquptLoginParamsData(
    username: String,
    password: String,
    device: DormnetDevice,
    val operator: CquptOperator,
) : UserPwdDeviceTargetParamsData(username, password, device)

class CquptViewModel : UserPwdDeviceModel() {
    var operator by mutableStateOf(CquptOperator.Telecom)

    override fun createLoginParams(): CquptLoginParamsData {
        return CquptLoginParamsData(
            username = username,
            password = password,
            device = device,
            operator = operator,
        )
    }
}

@DormnetTargetEntry
object CQUPT : UserPwdDeviceTarget() {
    override val title: StringResource = Res.string.school_cqupt

    @Composable
    override fun invoke() {
        super.invoke()

        val viewModel = viewModel<CquptViewModel>()
        Card {
            OverlayDropdownPreference(
                title = stringResource(Res.string.school_cqupt_operator),
                items = CquptOperator.entries.map { it.label },
                selectedIndex = viewModel.operator.ordinal,
                onSelectedIndexChange = {
                    viewModel.operator = CquptOperator.entries[it]
                },
            )
        }
        
        LaunchedEffect(Unit) {
            viewModel.username = Config[CquptUsernameKey] ?: ""
            viewModel.password = Config[CquptPasswordKey] ?: ""
            Config[CquptDeviceKey].parse<DormnetDevice> {
                viewModel.device = it
            }
            Config[CquptOperatorKey].parse<CquptOperator> {
                viewModel.operator = it
            }
        }
    }

    override fun createViewModel(): CquptViewModel {
        return CquptViewModel()
    }

    override suspend fun doLogin(params: UserPwdDeviceTargetParamsData): Result<String> {
        val userInfo = params as? CquptLoginParamsData
            ?: return Result.failure(IllegalArgumentException("CQUPT login params type mismatch."))

        Config {
            it[CquptUsernameKey] = params.username
            it[CquptPasswordKey] = params.password
            it[CquptDeviceKey] = params.device.name
            it[CquptOperatorKey] = params.operator.name
        }

        return runCatching {
            val netParams = requestLoginParameters().getOrThrow()
            val response = requestLogin(
                userInfo = userInfo,
                netParams = netParams,
            )
            val message = response.message ?: getString(Res.string.login_failed)
            check(response.result == "1") { message }
            message
        }
    }

    private suspend fun failedResult(key: String): Result<CquptNetworkInfo> {
        return Result.failure(IllegalStateException(getString(
            Res.string.school_cqupt_failed_redirect_info, key
        )))
    }
    private suspend fun requestLoginParameters(): Result<CquptNetworkInfo> {
        val response = CQCAI.HttpClient.get("http://192.168.198.1")

        val redirectUrl = response.headers[HttpHeaders.Location] ?: return failedResult("Location")
        val params = Url(redirectUrl).parameters
        val wlanuserip = params["wlanuserip"] ?: return failedResult("wlanuserip")
        val wlanacname = params["wlanacname"] ?: return failedResult("wlanacname")
        val wlanacip = params["wlanacip"] ?: return failedResult("wlanacip")
        val mac = params["mac"] ?: return failedResult("mac")
        return Result.success(CquptNetworkInfo(
            wlanuserip = wlanuserip,
            wlanacname = wlanacname,
            wlanacip = wlanacip,
            mac = mac,
        ))
    }

    private suspend fun requestLogin(
        userInfo: CquptLoginParamsData,
        netParams: CquptNetworkInfo,
    ): CquptEportalResponse {
        val response = HttpClient.get("http://192.168.200.2:801/eportal/") {
            parameter("c", "Portal")
            parameter("a", "login")
            parameter("callback", userInfo.device.callback)
            parameter("login_method", "1")
            parameter(
                "user_account",
                ",${userInfo.device.accountPrefix},${userInfo.username}@${userInfo.operator.value}",
            )
            parameter("user_password", userInfo.password)
            parameter("wlan_user_ip", netParams.wlanuserip)
            parameter("wlan_user_ipv6", "")
            parameter("wlan_user_mac", netParams.mac)
            parameter("wlan_ac_ip", "")
            parameter("wlan_ac_name", "")
            parameter("jsVersion", "3.3.3")
            header(HttpHeaders.UserAgent, userInfo.device.userAgent)
            header(HttpHeaders.Referrer, "http://192.168.200.2/")
            header("DNT", "1")
        }

        val body = response.bodyAsText()
        check(body.length > 2) { "Empty response of login request." }
        val json = body.substringAfter("(", missingDelimiterValue = body)
            .substringBeforeLast(")")
        val obj = Json.parseToJsonElement(json).jsonObject
        return CquptEportalResponse(
            result = obj["result"]?.jsonPrimitive?.content,
            message = obj["msg"]?.jsonPrimitive?.content,
        )
    }
}

private data class CquptEportalResponse(
    val result: String?,
    val message: String?,
)

private data class CquptNetworkInfo(
    val wlanuserip: String,
    val wlanacname: String,
    val wlanacip: String,
    val mac: String,
)

enum class CquptOperator(
    val value: String,
    val label: String,
) {
    Telecom("telecom", "中国电信"),
    Cmcc("cmcc", "中国移动"),
    Unicom("unicom", "中国联通"),
}

private val DormnetDevice.accountPrefix: String get() = when (this) {
    DormnetDevice.PC -> "0"
    DormnetDevice.Mobile -> "1"
}

private val DormnetDevice.callback: String get() = when (this) {
    DormnetDevice.PC -> "dr1003"
    DormnetDevice.Mobile -> "dr1005"
}

private val DormnetDevice.userAgent: String get() = when (this) {
    DormnetDevice.PC -> "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 Edg/141.0.0.0"
    DormnetDevice.Mobile -> "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Mobile Safari/537.36 EdgA/141.0.0.0"
}

private val CquptUsernameKey = stringPreferencesKey("cqupt_username")
private val CquptPasswordKey = stringPreferencesKey("cqupt_password")
private val CquptDeviceKey = stringPreferencesKey("cqupt_device")
private val CquptOperatorKey = stringPreferencesKey("cqupt_operator")
