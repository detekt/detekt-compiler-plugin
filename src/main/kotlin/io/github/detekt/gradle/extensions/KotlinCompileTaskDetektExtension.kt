package io.github.detekt.gradle.extensions

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

open class KotlinCompileTaskDetektExtension(project: Project) {
    val reports = project.container(DetektReport::class.java)

    init {
        reports.create("xml")
        reports.create("txt")
        reports.create("html")
        reports.create("sarif") { it.enabled.set(false) }
    }

    fun getXml() = reports.getByName("xml")
    fun getHtml() = reports.getByName("html")
    fun getTxt() = reports.getByName("txt")
    fun getSarif() = reports.getByName("sarif")

    private val objects: ObjectFactory = project.objects

    val enabled: Property<Boolean> = objects.property(Boolean::class.java)
    val baseline: RegularFileProperty = objects.fileProperty()
    val debug: Property<Boolean> = objects.property(Boolean::class.java)
    val buildUponDefaultConfig: Property<Boolean> = objects.property(Boolean::class.java)
    val config: ConfigurableFileCollection = objects.fileCollection()
    val excludes: SetProperty<String> = objects.setProperty(String::class.java)

}
