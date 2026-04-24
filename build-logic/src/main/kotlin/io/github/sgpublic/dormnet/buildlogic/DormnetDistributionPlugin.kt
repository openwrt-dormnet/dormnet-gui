package io.github.sgpublic.dormnet.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class DormnetDistributionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.rootProject.tasks.register("packageDistributions") {
            group = GROUP
            doFirst {

            }
        }
    }

    companion object {
        private const val GROUP = "distribution"
    }
}
