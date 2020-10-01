package io.github.detekt.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class DetektGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // See DetektKotlinCompilerPlugin
        target.pluginManager.apply(DETEKT_COMPILER_PLUGIN)
    }
}
