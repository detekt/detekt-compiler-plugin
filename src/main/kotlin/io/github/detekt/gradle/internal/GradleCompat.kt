package io.github.detekt.gradle.internal

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.util.GradleVersion

@Suppress("DEPRECATION")
internal fun Project.configurableFileCollection(): ConfigurableFileCollection =
    if (GradleVersion.current() < GradleVersion.version("5.3")) {
        layout.configurableFiles()
    } else {
        objects.fileCollection()
    }
