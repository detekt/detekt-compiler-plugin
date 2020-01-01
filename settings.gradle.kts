rootProject.name = "detekt-compiler-plugin"

pluginManagement {
    val artifactoryVersion: String by settings
    val bintrayVersion: String by settings
    val gradleVersionsPluginVersion: String by settings
    val kotlinVersion: String by settings
    val detektVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        id("io.gitlab.arturbosch.detekt") version detektVersion
        id("com.jfrog.artifactory") version artifactoryVersion
        id("com.jfrog.bintray") version bintrayVersion
        id("com.github.ben-manes.versions") version gradleVersionsPluginVersion
    }
}
