package io.github.detekt.gradle

import io.github.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.api.reporting.ReportingExtension

class DetektGradlePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.pluginManager.apply(ReportingBasePlugin::class.java)
        val extension = target.extensions.create(DETEKT_NAME, DetektExtension::class.java, target)
        extension.reportsDir = target.extensions.getByType(ReportingExtension::class.java).file(DETEKT_NAME)

        val defaultConfigFile = getDefaultConfigFile(target)

        if (defaultConfigFile.exists()) {
            extension.config = target.files(defaultConfigFile)
        }
    }

    private fun getDefaultConfigFile(target: Project) =
        target.file("${target.rootProject.layout.projectDirectory.dir(CONFIG_DIR_NAME)}/$CONFIG_FILE")
}
