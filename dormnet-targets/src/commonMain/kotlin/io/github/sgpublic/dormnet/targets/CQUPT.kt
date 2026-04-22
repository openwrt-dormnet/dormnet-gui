package io.github.sgpublic.dormnet.targets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.sgpublic.dormnet.core.HttpClient
import io.github.sgpublic.dormnet.targets.core.DormnetTargetEntry
import io.github.sgpublic.dormnet.targets.template.DormnetDevice
import io.github.sgpublic.dormnet.targets.template.UserPwdDeviceTarget
import io.github.sgpublic.dormnet.targets.template.UserPwdDeviceModel
import io.github.sgpublic.dormnet.targets.template.UserPwdDeviceTargetParamsData
import io.ktor.client.plugins.resources.get
import io.ktor.client.request.cookie
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.resources.Resource
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.StringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference

private const val PortalReferer = "http://192.168.200.2/"

@Resource(PortalReferer)
private class CquptPageTypeDataResource(
    val c: String = "Portal",
    val a: String = "page_type_data",
)

@Resource("http://192.168.200.2:801/eportal/")
private class CquptLoginResource(
    val callback: String,
    val user_account: String,
    val user_password: String,
    val wlan_user_ip: String,
    val wlan_user_mac: String,
    val c: String = "Portal",
    val a: String = "login",
    val login_method: String = "1",
    val wlan_user_ipv6: String = "",
    val wlan_ac_ip: String = "",
    val wlan_ac_name: String = "",
    val jsVersion: String = "3.3.3",
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
    override val title: StringResource = Res.string.school_cqcai

    @Composable
    override fun invoke() {
        super.invoke()

        val viewModel = viewModel<CquptViewModel>()
        Card {
            OverlayDropdownPreference(
                title = "运营商",
                items = CquptOperator.entries.map { it.label },
                selectedIndex = viewModel.operator.ordinal,
                onSelectedIndexChange = {
                    viewModel.operator = CquptOperator.entries[it]
                },
            )
        }
    }

    override fun createViewModel(): CquptViewModel {
        return CquptViewModel()
    }

    override suspend fun doLogin(params: UserPwdDeviceTargetParamsData): Result<String> {
        val cquptParams = params as? CquptLoginParamsData
            ?: return Result.failure(IllegalArgumentException("CQUPT login params type mismatch."))

        return runCatching {
            val session = requestSession(cquptParams.device)
            val response = requestLogin(
                params = cquptParams,
                ip = "0.0.0.0",
                mac = "00:00:00:00:00:00",
                phpSessionId = session,
            )
            val message = response.message ?: getString(Res.string.login_failed)
            check(response.result == "1") { message }
            message
        }
    }

    private suspend fun requestSession(device: DormnetDevice): String {
        val response = HttpClient.get(CquptPageTypeDataResource()) {
            header(HttpHeaders.UserAgent, device.userAgent)
            header(HttpHeaders.Referrer, PortalReferer)
            header("DNT", "1")
        }

        return response.headers
            .getAll(HttpHeaders.SetCookie)
            ?.firstNotNullOfOrNull { cookie ->
                Regex("""(?:^|;\s*)PHPSESSID=([^;]+)""").find(cookie)?.groupValues?.get(1)
            }
            ?: error("Unable to find cookie PHPSESSID.")
    }

    private suspend fun requestLogin(
        params: CquptLoginParamsData,
        ip: String,
        mac: String,
        phpSessionId: String,
    ): CquptEportalResponse {
        val response = HttpClient.get(
            CquptLoginResource(
                callback = params.device.callback,
                user_account = ",${params.device.accountPrefix},${params.username}@${params.operator.value}",
                user_password = params.password,
                wlan_user_ip = ip,
                wlan_user_mac = mac,
            )
        ) {
            cookie("PHPSESSID", phpSessionId)
            header(HttpHeaders.UserAgent, params.device.userAgent)
            header(HttpHeaders.Referrer, PortalReferer)
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

data class CquptNetworkInfo(
    val ip: String?,
    val mac: String?,
)
