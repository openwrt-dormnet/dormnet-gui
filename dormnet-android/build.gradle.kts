import com.android.build.api.variant.FilterConfiguration
import kotlin.io.encoding.Base64
import io.github.sgpublic.dormnet.buildlogic.ciArtifactsDir
import org.gradle.internal.os.OperatingSystem
import org.gradle.api.tasks.Copy
import java.io.Serializable

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = libs.versions.app.packageName.get()
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = libs.versions.app.packageName.get()
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = libs.versions.app.versionCode.get().toInt()
        versionName = libs.versions.app.versionName.get()
    }
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = false
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    val releaseKeyPassword = providers.environmentVariable("KEY_PASSWORD").orNull
    val releaseKeyAlias = providers.environmentVariable("KEY_ALIAS").orNull
    val releaseKeyContent = providers.environmentVariable("KEY_CONTENT").orNull
    val hasSigningKey = releaseKeyPassword != null && releaseKeyAlias != null && releaseKeyContent != null
    val dormnet by signingConfigs.register("dormnet") {
        storePassword = releaseKeyPassword
        keyPassword = releaseKeyPassword
        keyAlias = releaseKeyAlias
        storeFile = releaseKeyContent?.let {
            layout.buildDirectory.file("keys.jks").get().asFile.apply {
                parentFile.mkdirs()
                writeBytes(Base64.decode(it))
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            if (hasSigningKey) {
                signingConfig = dormnet
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }
}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            val abi = output.filters
                .firstOrNull { it.filterType == FilterConfiguration.FilterType.ABI }
                ?.identifier
                ?: "universal"
            output.outputFileName.set(
                "dormnet-v${libs.versions.app.versionName.get()}-android-$abi.apk"
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.components.resources)
    implementation(projects.dormnetCore)
    implementation(projects.dormnetShared)
}

val copyAndroidReleaseArtifacts by tasks.registering(Copy::class) {
    group = "distribution"
    description = "Copies Android release APK and AAB artifacts into the CI artifact directory."
    dependsOn("assembleRelease")

    from(layout.buildDirectory.dir("outputs"))
    include("apk/release/**/*.apk")
    eachFile {
        relativePath = RelativePath(true, name)
    }
    into(ciArtifactsDir())
}

if (OperatingSystem.current().isMacOsX) {
    rootProject.tasks.named("packageDistributions") {
        dependsOn(copyAndroidReleaseArtifacts)
    }
}
