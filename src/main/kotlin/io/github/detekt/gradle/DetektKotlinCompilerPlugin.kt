package io.github.detekt.gradle

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
    ): List<SubpluginOption> = emptyList()

    override fun getCompilerPluginId(): String = "detekt-compiler-plugin"

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact("io.github.detekt", "detekt-compiler-plugin", "0.2.0") // TODO: generate version

    override fun isApplicable(project: Project, task: AbstractCompile): Boolean = true
}
