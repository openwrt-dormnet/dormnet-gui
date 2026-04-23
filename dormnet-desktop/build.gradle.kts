import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

dependencies {
    implementation(projects.dormnetShared)
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)
}

compose.desktop {
    application {
        mainClass = "io.github.sgpublic.dormnet.MainKt"

        nativeDistributions {
            targetFormats(
                TargetFormat.Dmg, TargetFormat.Pkg,
                TargetFormat.Msi, TargetFormat.Exe,
                TargetFormat.Deb, TargetFormat.Rpm, TargetFormat.AppImage,
            )
            packageName = "DormNet"
            packageVersion = libs.versions.app.versionName.get()

            macOS {
                iconFile = file("./icons/ic_launcher-macos.icns")
                appCategory = "public.app-category.utilities"
            }
            windows {
                iconFile = file("./icons/ic_launcher-windows.ico")
                upgradeUuid = "bf69482f-346f-4711-8995-d566d56aa655"
                perUserInstall = true
            }
            linux {
                iconFile = file("./icons/ic_launcher-linux.png")
                appCategory = "Utility"
            }
        }
    }
}
