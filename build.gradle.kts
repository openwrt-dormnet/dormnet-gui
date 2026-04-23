plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.plugin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktor) apply false
}

tasks {
    val syncIosVersionConfig by registering {
        group = "configuration"
        description = "Syncs iOS version fields in Config.xcconfig from gradle/libs.versions.toml."

        val iosConfigFile = project.file("dormnet-ios/Configuration/Config.xcconfig")
        val versionCatalog = project.extensions.getByType<VersionCatalogsExtension>().named("libs")
        val appVersionCode = versionCatalog.findVersion("app-versionCode").get().requiredVersion
        val appVersionName = versionCatalog.findVersion("app-versionName").get().requiredVersion

        inputs.file(iosConfigFile)
        inputs.property("appVersionCode", appVersionCode)
        inputs.property("appVersionName", appVersionName)

        doLast {
            if (!iosConfigFile.isFile) {
                return@doLast
            }

            val currentContent = iosConfigFile.readText()
            val syncedContent = currentContent
                .replace(
                    Regex("(?m)^CURRENT_PROJECT_VERSION=.*$"),
                    "CURRENT_PROJECT_VERSION=$appVersionCode",
                )
                .replace(
                    Regex("(?m)^MARKETING_VERSION=.*$"),
                    "MARKETING_VERSION=$appVersionName",
                )

            if (syncedContent != currentContent) {
                iosConfigFile.writeText(syncedContent)
            }
        }
    }
}
