plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

kotlin {
    android {
        namespace = "${libs.versions.app.packageName.get()}.targets.core"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    iosArm64()
    iosSimulatorArm64()
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.components.resources)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(projects.dormnetCore)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
