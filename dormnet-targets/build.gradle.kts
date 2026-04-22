plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

kotlin {
    android {
        namespace = "${libs.versions.app.packageName.get()}.targets"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources {
            enable = true
        }
    }

    iosArm64()
    iosSimulatorArm64()
    jvm()

    sourceSets {
        commonMain {
            kotlin.srcDir(layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin"))
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.bundles.miuix)
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.bundles.androidx.datastore)
            implementation(libs.bundles.ktor.client)
            implementation(projects.dormnetCore)
            implementation(projects.dormnetTargetsCore)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", projects.dormnetTargetsProcessor)
}

tasks.matching {
    (it.name.startsWith("compile") && it.name.contains("Kotlin")) ||
        it.name == "compileAndroidMain"
}.configureEach {
    dependsOn("kspCommonMainKotlinMetadata")
}

compose.resources {
    packageOfResClass = "${libs.versions.app.packageName.get()}.targets"
    publicResClass = true
}
