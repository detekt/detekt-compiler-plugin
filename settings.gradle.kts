rootProject.name = "detekt-compiler-plugin"

pluginManagement {
    val downloadVersion: String by settings
    val gradleVersionsPluginVersion: String by settings
    val kotlinVersion: String by settings
    val shadowVersion: String by settings

    repositories {
        gradlePluginPortal()
        mavenLocal()
        maven { setUrl("https://dl.bintray.com/arturbosch/code-analysis") }
    }

    plugins {
        kotlin("jvm") version kotlinVersion
        id("io.github.detekt.gradle.compiler-plugin") version "0.3.0"
        id("com.gradle.plugin-publish") version "0.11.0"
        id("com.github.ben-manes.versions") version gradleVersionsPluginVersion
        id("com.github.johnrengelman.shadow") version shadowVersion
        id("de.undercouch.download") version downloadVersion
    }
}
