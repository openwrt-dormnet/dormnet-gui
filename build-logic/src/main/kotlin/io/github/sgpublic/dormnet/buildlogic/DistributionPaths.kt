package io.github.sgpublic.dormnet.buildlogic

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

fun Project.ciArtifactsDir(): Provider<Directory> =
    rootProject.layout.buildDirectory.dir("ci-artifacts")
