package io.github.detekt.gradle

import io.github.detekt.compiler.plugin.Options
import io.github.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import java.io.File
import java.security.MessageDigest
import java.util.Base64

class DetektKotlinCompilerPlugin : KotlinCompilerPluginSupportPlugin {

    private lateinit var project: Project

    override fun apply(target: Project) {
        project = target
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val extension = project.extensions
            .findByType(DetektExtension::class.java)
            ?: DetektExtension(project)

        val options = project.objects.listProperty(SubpluginOption::class.java).apply {
            add(SubpluginOption(Options.debug, extension.debug.toString()))
            add(SubpluginOption(Options.configDigest, extension.config.toDigest()))
            add(SubpluginOption(Options.isEnabled, extension.isEnabled.toString()))
            add(SubpluginOption(Options.useDefaultConfig, extension.buildUponDefaultConfig.toString()))
        }

        extension.baseline?.let { options.add(SubpluginOption(Options.baseline, it.toString())) }
        if (extension.config.any()) {
            options.add(SubpluginOption(Options.config, extension.config.joinToString(",")))
        }

        return options
    }

    override fun getCompilerPluginId(): String = DETEKT_COMPILER_PLUGIN

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact("io.github.detekt", "detekt-compiler-plugin", "0.2.0") // TODO: generate version

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true
}

internal fun ConfigurableFileCollection.toDigest(): String {
    val concatenatedConfig = this
        .filter { it.isFile }
        .map(File::readBytes)
        .fold(byteArrayOf()) { acc, file -> acc + file }

    return Base64.getEncoder().encodeToString(
        MessageDigest.getInstance("SHA-256").digest(concatenatedConfig)
    )
}
