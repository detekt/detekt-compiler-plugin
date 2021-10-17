plugins {
    kotlin("jvm") version "1.5.31"
    id("io.github.detekt.gradle.compiler-plugin")
}

detekt {
    debug.set(true)
    isEnabled.set(true)
    allRules.set(true)
    parallel.set(true)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        allWarningsAsErrors = true
    }
}

repositories {
    mavenCentral()
}
