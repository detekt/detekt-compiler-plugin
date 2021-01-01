package io.github.detekt.gradle

import io.github.detekt.compiler.plugin.Options
import io.github.detekt.gradle.extensions.ProjectDetektExtension
import io.github.detekt.gradle.extensions.KotlinCompileTaskDetektExtension
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.api.provider.Provider
import org.gradle.api.reporting.ReportingExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.ObjectOutputStream
import java.security.MessageDigest
import java.util.Base64

class DetektKotlinCompilerPlugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        target.pluginManager.apply(ReportingBasePlugin::class.java)
        val extension = target.extensions.create(DETEKT_NAME, ProjectDetektExtension::class.java)
        extension.reportsDir = target.extensions.getByType(ReportingExtension::class.java).file(DETEKT_NAME)
        extension.excludes.add("**/${target.relativePath(target.buildDir)}/**")

        extension.isEnabled.convention(true)
        extension.debug.convention(false)
        extension.buildUponDefaultConfig.convention(true)

        val defaultConfigFile = getDefaultConfigFile(target)

        if (defaultConfigFile.exists()) {
            extension.config.setFrom(target.files(defaultConfigFile))
        }

        target.configurations.create(CONFIGURATION_DETEKT_PLUGINS) { configuration ->
            configuration.isVisible = false
            configuration.isTransitive = true
            configuration.description = "The $CONFIGURATION_DETEKT_PLUGINS libraries to be used for this project."
        }

        target.tasks.withType(KotlinCompile::class.java).configureEach { task ->
            task.extensions.create(DETEKT_NAME, KotlinCompileTaskDetektExtension::class.java, target).apply {
                enabled.convention(extension.isEnabled)
                baseline.convention(extension.baseline)
                debug.convention(extension.debug)
                buildUponDefaultConfig.convention(extension.buildUponDefaultConfig)
                config.setFrom(target.files(defaultConfigFile))
                excludes.convention(extension.excludes)
            }
        }
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val providers = project.providers

        val extension = project.extensions.getByType(ProjectDetektExtension::class.java)
        val taskExtension = kotlinCompilation.compileKotlinTask.extensions.getByType(KotlinCompileTaskDetektExtension::class.java)

        val reportsDir: Provider<RegularFile> = project.layout.file(providers.provider { extension.reportsDir })

        project.configurations.getByName("kotlinCompilerPluginClasspath").apply {
            extendsFrom(project.configurations.getAt(CONFIGURATION_DETEKT_PLUGINS))
        }

        val options = project.objects.listProperty(SubpluginOption::class.java).apply {
            add(SubpluginOption(Options.debug, taskExtension.debug.get().toString()))
            add(SubpluginOption(Options.configDigest, taskExtension.config.toDigest()))
            add(SubpluginOption(Options.isEnabled, taskExtension.enabled.get().toString()))
            add(SubpluginOption(Options.useDefaultConfig, taskExtension.buildUponDefaultConfig.get().toString()))
            add(SubpluginOption(Options.rootPath, project.rootDir.toString()))
            add(SubpluginOption(Options.excludes, taskExtension.excludes.get().encodeToBase64()))

            taskExtension.reports.all { report ->
                report.enabled.convention(true)
                report.destination.convention(
                    project.layout.projectDirectory.file(providers.provider {
                        val reportFileName = "${kotlinCompilation.name}.${report.name}"
                        File(reportsDir.get().asFile, reportFileName).absolutePath
                    })
                )

                if (report.enabled.get()) {
                    add(SubpluginOption(Options.report, "${report.name}:${report.destination.asFile.get().absolutePath}"))
                }
            }
        }

        taskExtension.baseline.getOrNull()?.let { options.add(SubpluginOption(Options.baseline, it.toString())) }
        if (taskExtension.config.any()) {
            options.add(SubpluginOption(Options.config, taskExtension.config.joinToString(",")))
        }

        return options
    }

    override fun getCompilerPluginId(): String = DETEKT_COMPILER_PLUGIN

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact("io.github.detekt", "detekt-compiler-plugin")

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

private fun Set<String>.encodeToBase64(): String {
    val os = ByteArrayOutputStream()

    ObjectOutputStream(os).use { oos ->
        oos.writeInt(size)
        forEach(oos::writeUTF)
        oos.flush()
    }

    return Base64.getEncoder().encodeToString(os.toByteArray())
}
