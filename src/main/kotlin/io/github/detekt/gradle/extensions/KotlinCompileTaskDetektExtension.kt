package io.github.detekt.gradle.extensions

import org.gradle.api.Project

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
}
