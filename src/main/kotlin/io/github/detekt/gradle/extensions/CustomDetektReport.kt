package io.github.detekt.gradle.extensions

import java.io.File

class CustomDetektReport {

    var reportId: String? = null
    var destination: File? = null

    override fun toString(): String = "CustomDetektReport(reportId=$reportId, destination=$destination)"
}
