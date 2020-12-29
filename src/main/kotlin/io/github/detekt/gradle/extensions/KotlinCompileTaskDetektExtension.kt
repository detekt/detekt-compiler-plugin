package io.github.detekt.gradle.extensions

import org.gradle.api.Project

open class KotlinCompileTaskDetektExtension(project: Project) {
    val reports = project.container(DetektReport::class.java)

    init {
        reports.create("xml")
        reports.create("txt")
        reports.create("html")
    }

    fun getXml() = reports.getByName("xml")
    fun getHtml() = reports.getByName("html")
    fun getTxt() = reports.getByName("txt")
}
