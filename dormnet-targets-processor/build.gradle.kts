plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.ksp.symbol.processing.api)
    implementation(projects.dormnetTargetsCore)
}
