package io.github.sgpublic.dormnet.targets

import io.github.sgpublic.dormnet.targets.core.DormnetTargetEntry
import io.github.sgpublic.dormnet.targets.template.UserPwdDeviceTarget
import io.github.sgpublic.dormnet.targets.template.UserPwdDeviceTargetParamsData
import org.jetbrains.compose.resources.StringResource

@DormnetTargetEntry
object CQCAI : UserPwdDeviceTarget() {
    override val title: StringResource = Res.string.school_cqupt

    override suspend fun doLogin(params: UserPwdDeviceTargetParamsData): Result<String> {
        return Result.success("")
    }
}
