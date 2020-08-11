package io.github.detekt.gradle

import io.github.detekt.compiler.plugin.Options
import io.github.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.api.tasks.compile.AbstractCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinGradleSubplugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class DetektKotlinCompilerPlugin : KotlinGradleSubplugin<AbstractCompile> {

    override fun apply(
        project: Project,
        kotlinCompile: AbstractCompile,
        javaCompile: AbstractCompile?,
        variantData: Any?,
        androidProjectHandler: Any?,
        kotlinCompilation: KotlinCompilation<KotlinCommonOptions>?
    ): List<SubpluginOption> {
        val extension = project.extensions
            .findByType(DetektExtension::class.java)
            ?: DetektExtension(project)

        val options = mutableListOf(
            SubpluginOption(Options.debug, extension.debug.toString()),
            SubpluginOption(Options.config, extension.config.joinToString(",")),
            SubpluginOption(Options.isEnabled, extension.isEnabled.toString()),
            SubpluginOption(Options.useDefaultConfig, extension.buildUponDefaultConfig.toString())
        )

        extension.baseline?.let { options.add(SubpluginOption(Options.baseline, it.toString())) }

        return options
    }

    override fun getCompilerPluginId(): String = DETEKT_COMPILER_PLUGIN

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact("io.github.detekt", "detekt-compiler-plugin", "0.2.0") // TODO: generate version

    override fun isApplicable(project: Project, task: AbstractCompile): Boolean = true
}
