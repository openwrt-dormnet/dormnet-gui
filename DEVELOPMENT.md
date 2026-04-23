# 学校适配指南

## 写给用户

> 待完善

## 写给开发者

学校适配位于 `dormnet-targets/src/commonMain/kotlin/io/github/sgpublic/dormnet/targets`。

新增学校时通常需要：

1. 新建一个继承 `DormnetTarget`、`UserPwdTarget` 或 `UserPwdDeviceTarget` 的目标实现。
2. 使用 `@DormnetTargetEntry(maintainer = ["github-id"])` 标记该实现。
3. 在 `dormnet-targets/src/commonMain/composeResources/values/strings.xml` 添加学校名称。
4. 实现登录参数保存、认证请求和返回信息处理。

示例：

```kotlin
@DormnetTargetEntry(
    maintainer = ["your-github-id"]
)
object ExampleUniversity : UserPwdDeviceTarget() {
    override val title: StringResource = Res.string.school_example

    override suspend fun doLogin(params: UserPwdDeviceTargetParamsData): Result<String> {
        TODO("Implement login request")
    }
}
```

