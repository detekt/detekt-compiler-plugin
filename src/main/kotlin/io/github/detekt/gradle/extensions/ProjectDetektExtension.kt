package io.github.detekt.gradle.extensions

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.quality.CodeQualityExtension
import org.gradle.api.provider.SetProperty
import java.io.File
import javax.inject.Inject

open class ProjectDetektExtension constructor(@Inject val objects: ObjectFactory) : CodeQualityExtension() {

    var isEnabled: Boolean = true
    var baseline: File? = null
    var debug: Boolean = false
    var buildUponDefaultConfig: Boolean = true

    val config: ConfigurableFileCollection = objects.fileCollection()
    val excludes: SetProperty<String> = objects.setProperty(String::class.java)

}
