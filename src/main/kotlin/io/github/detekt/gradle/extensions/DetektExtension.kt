package io.github.detekt.gradle.extensions

import org.gradle.api.Action
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.quality.CodeQualityExtension
import org.gradle.api.provider.SetProperty
import java.io.File
import javax.inject.Inject

open class DetektExtension constructor(@Inject val objects: ObjectFactory) : CodeQualityExtension() {

    var isEnabled: Boolean = true
    var baseline: File? = null
    var debug: Boolean = false
    var parallel: Boolean = false
    var failFast: Boolean = false
    var buildUponDefaultConfig: Boolean = true
    var disableDefaultRuleSets: Boolean = false
    var autoCorrect: Boolean = false

    var config: ConfigurableFileCollection = objects.fileCollection()
    val excludes: SetProperty<String> = objects.setProperty(String::class.java)

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
