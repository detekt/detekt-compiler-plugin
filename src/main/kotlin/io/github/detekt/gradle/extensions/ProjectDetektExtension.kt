package io.github.detekt.gradle.extensions

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.quality.CodeQualityExtension
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import javax.inject.Inject

open class ProjectDetektExtension constructor(@Inject val objects: ObjectFactory) : CodeQualityExtension() {

    val isEnabled: Property<Boolean> = objects.property(Boolean::class.java)
    val baseline: RegularFileProperty = objects.fileProperty()
    val debug: Property<Boolean> = objects.property(Boolean::class.java)
    val buildUponDefaultConfig: Property<Boolean> = objects.property(Boolean::class.java)

    val config: ConfigurableFileCollection = objects.fileCollection()
    val excludes: SetProperty<String> = objects.setProperty(String::class.java)

}
