package io.github.detekt.gradle

import io.github.detekt.compiler.plugin.Options
import io.github.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.api.provider.Provider
import org.gradle.api.reporting.ReportingExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import java.io.File
import java.security.MessageDigest
import java.util.Base64

class DetektKotlinCompilerPlugin : KotlinCompilerPluginSupportPlugin {

    private lateinit var project: Project

    override fun apply(target: Project) {
        project = target

        target.pluginManager.apply(ReportingBasePlugin::class.java)
        val extension = target.extensions.create(DETEKT_NAME, DetektExtension::class.java, target)
        extension.reportsDir = target.extensions.getByType(ReportingExtension::class.java).file(DETEKT_NAME)

        val defaultConfigFile = getDefaultConfigFile(target)

        if (defaultConfigFile.exists()) {
            extension.config = target.files(defaultConfigFile)
        }
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val extension = project.extensions.getByType(DetektExtension::class.java)

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
        SubpluginArtifact("io.github.detekt", "detekt-compiler-plugin", "0.4.0") // TODO: generate version

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean =
        kotlinCompilation.platformType in setOf(KotlinPlatformType.jvm, KotlinPlatformType.androidJvm)

    private fun getDefaultConfigFile(target: Project) =
        target.file("${target.rootProject.layout.projectDirectory.dir(CONFIG_DIR_NAME)}/$CONFIG_FILE")
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
