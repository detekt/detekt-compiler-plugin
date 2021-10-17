plugins {
    alias(libs.plugins.kotlin)
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
