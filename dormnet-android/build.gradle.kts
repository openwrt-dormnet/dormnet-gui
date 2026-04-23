import kotlin.io.encoding.Base64

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
    val dormnet by signingConfigs.register("dormnet") {
        storePassword = providers.environmentVariable("KEY_PASSWORD").orNull
        keyPassword = providers.environmentVariable("KEY_PASSWORD").orNull
        keyAlias = providers.environmentVariable("KEY_ALIAS").orNull
        storeFile = provider {
            val file = layout.buildDirectory.file("keys.jks").get().asFile
            providers.environmentVariable("KEY_CONTENT").orNull?.let {
                file.createNewFile()
                file.writeBytes(Base64.decode(it))
            }
            file
        }.orNull
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = dormnet
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
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
