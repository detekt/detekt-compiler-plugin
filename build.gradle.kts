plugins {
    kotlin("jvm") version "1.4.32"
    id("io.github.detekt.gradle.compiler-plugin")
}

detekt {
    debug.set(true)
    isEnabled.set(true)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        allWarningsAsErrors = true
    }
}

repositories {
    mavenCentral()
}
