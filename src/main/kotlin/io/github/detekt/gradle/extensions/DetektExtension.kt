package io.github.detekt.gradle.extensions

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.plugins.quality.CodeQualityExtension
import java.io.File

open class DetektExtension(project: Project) : CodeQualityExtension() {

    var isEnabled: Boolean = true
    var baseline: File? = null
    var debug: Boolean = false
    var parallel: Boolean = false
    var failFast: Boolean = false
    var buildUponDefaultConfig: Boolean = true
    var disableDefaultRuleSets: Boolean = false
    var autoCorrect: Boolean = false

    var config: ConfigurableFileCollection = project.objects.fileCollection()

    var ignoreFailures: Boolean
        @JvmName("ignoreFailures_")
        get() = isIgnoreFailures
        @JvmName("ignoreFailures_")
        set(value) {
            isIgnoreFailures = value
        }

    val reports = DetektReports()

    fun reports(configure: Action<DetektReports>) = configure.execute(reports)
}
