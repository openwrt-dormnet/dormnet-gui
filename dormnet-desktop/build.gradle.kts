import io.github.sgpublic.dormnet.buildlogic.ciArtifactsDir
import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.gradle.internal.os.OperatingSystem
import org.gradle.api.Action
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.tasks.Copy
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.io.Serializable

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

val copyDesktopReleaseArtifacts by tasks.registering(Copy::class) {
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
    eachFile(DesktopArtifactRenameAction(versionName))
    includeEmptyDirs = false
    into(ciArtifactsDir())
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
                    copyDesktopReleaseArtifacts.dependsOn("package${format.name}")
                }
                rootProject.tasks.named("packageDistributions") {
                    dependsOn(copyDesktopReleaseArtifacts)
                }
            }
            targetFormats(*formats)
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

private class DesktopArtifactRenameAction(
    private val versionName: String
): Action<FileCopyDetails>, Serializable {
    override fun execute(details: FileCopyDetails) {
        val extension = details.name.substringAfterLast('.', missingDelimiterValue = "")
        val newName = if (extension.isNotEmpty()) {
            "dormnet-v${versionName}-$osName-$archName.$extension"
        } else {
            "dormnet-v${versionName}-$osName-$archName"
        }
        details.name = newName
        details.relativePath = RelativePath(true, newName)
    }

    companion object {
        private val currentOs = OperatingSystem.current()
        private val osName = when {
            currentOs.isMacOsX -> "macos"
            currentOs.isWindows -> "windows"
            currentOs.isLinux -> "linux"
            else -> "unknown"
        }
        private val archName = System.getProperty("os.arch")
            .lowercase()
            .let {
                when (it) {
                    "aarch64", "arm64" -> "arm64"
                    "amd64", "x86_64" -> "x64"
                    else -> it
                }
            }
    }
}
