import io.github.sgpublic.dormnet.buildlogic.CiArtifactCopyTask
import org.gradle.internal.os.OperatingSystem
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

private val currentDesktopOs = OperatingSystem.current()
private val desktopOsName = when {
    currentDesktopOs.isMacOsX -> "macos"
    currentDesktopOs.isWindows -> "windows"
    currentDesktopOs.isLinux -> "linux"
    else -> "unknown"
}
private val desktopArchName = System.getProperty("os.arch")
    .lowercase()
    .let {
        when (it) {
            "aarch64", "arm64" -> "arm64"
            "amd64", "x86_64" -> "x64"
            else -> it
        }
    }

val copyDesktopReleaseArtifacts by tasks.registering(CiArtifactCopyTask::class) {
    group = "distribution"
    description = "Copies Desktop release installers into the CI artifact directory."

    from(layout.buildDirectory.dir("compose/binaries/main"))
    include("**/*.AppImage")
    include("**/*.deb")
    include("**/*.dmg")
    include("**/*.exe")
    include("**/*.msi")
    include("**/*.pkg")
    include("**/*.rpm")
    val versionName = libs.versions.app.versionName.get()
    renameArtifactsWithPrefix("dormnet-v$versionName-$desktopOsName-$desktopArchName")
    includeEmptyDirs = false
}

compose.desktop {
    application {
        mainClass = "io.github.sgpublic.dormnet.MainKt"

        nativeDistributions {
            val os = OperatingSystem.current()
            val formats = when {
                os.isMacOsX -> arrayOf(TargetFormat.Dmg, TargetFormat.Pkg)
                os.isWindows -> arrayOf(TargetFormat.Msi, TargetFormat.Exe)
                os.isLinux -> arrayOf(TargetFormat.Deb, TargetFormat.Rpm, TargetFormat.AppImage)
                else -> emptyArray()
            }
            if (formats.isNotEmpty()) {
                for (format in formats) {
                    copyDesktopReleaseArtifacts.configure {
                        dependsOn("package${format.name}")
                    }
                }
                rootProject.tasks.named("packageDistributions") {
                    dependsOn(copyDesktopReleaseArtifacts)
                }
            }
            targetFormats(*formats)
            packageName = "DormNet"
            packageVersion = libs.versions.app.versionName.get()

            modules("jdk.unsupported")
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
