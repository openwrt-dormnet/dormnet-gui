package io.github.sgpublic.dormnet.buildlogic

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.file.RelativePath
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import java.io.Serializable

abstract class CiArtifactCopyTask : Copy() {
    init {
        includeEmptyDirs = false
        into(project.rootProject.layout.buildDirectory.dir("ci-artifacts"))
    }

    fun flattenArtifacts() {
        eachFile(CiArtifactPathAction(fileNamePrefix = null))
    }

    fun renameArtifactsWithPrefix(fileNamePrefix: String) {
        eachFile(CiArtifactPathAction(fileNamePrefix))
    }
}

private class CiArtifactPathAction(
    private val fileNamePrefix: String?,
) : Action<FileCopyDetails>, Serializable {
    override fun execute(details: FileCopyDetails) {
        val extension = details.name.substringAfterLast('.', missingDelimiterValue = "")
        val targetName = when {
            fileNamePrefix.isNullOrBlank() -> details.name
            extension.isNotEmpty() -> "$fileNamePrefix.$extension"
            else -> fileNamePrefix
        }
        details.name = targetName
        details.relativePath = RelativePath(true, targetName)
    }
}
