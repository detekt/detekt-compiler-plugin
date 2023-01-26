import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion: String = libs.versions.kotlin.get()

val detektPluginVersion: String by project

group = "io.github.detekt"
version = detektPluginVersion

plugins {
    alias(libs.plugins.kotlin)
    id("maven-publish")
    id("signing")
    id("java-gradle-plugin")
    id("jvm-test-suite")
    alias(libs.plugins.pluginPublishing)
    alias(libs.plugins.gradleVersionz)
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(kotlin("gradle-plugin"))
    compileOnly(kotlin("gradle-plugin-api"))

    testImplementation(libs.assertj.core)
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf(
        "-opt-in=kotlin.RequiresOptIn"
    )
}

tasks.withType<Test>().configureEach {
    testLogging {
        // set options for log level LIFECYCLE
        events = setOf(
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR,
            org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT,
            org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
        )
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter(libs.versions.junit.get())
        }
    }
}

tasks {
    val writeDetektVersionProperties by registering(WriteProperties::class) {
        description = "Write the properties file with the Detekt version to be used by the plugin"
        encoding = "UTF-8"
        outputFile = file("$buildDir/versions.properties")
        property("detektCompilerPluginVersion", project.version)
    }

    processResources {
        from(writeDetektVersionProperties)
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    dependsOn(tasks.classes)
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    from(tasks.javadoc)
    archiveClassifier.set("javadoc")
}

artifacts {
    archives(sourcesJar)
    archives(javadocJar)
}

gradlePlugin {
    plugins {
        register("detektCompilerPlugin") {
            id = "io.github.detekt.gradle.compiler-plugin"
            implementationClass = "io.github.detekt.gradle.DetektKotlinCompilerPlugin"
        }
    }
}

pluginBundle {
    website = "https://detekt.github.io/detekt"
    vcsUrl = "https://github.com/detekt/detekt-compiler-plugin"
    description = "Static code analysis for Kotlin as a compiler plugin."
    tags = listOf("kotlin", "detekt", "code-analysis")

    (plugins) {
        "detektCompilerPlugin" {
            id = "io.github.detekt.gradle.compiler-plugin"
            displayName = "Static code analysis for Kotlin"
        }
    }
}

publishing {
    repositories {
        maven {
            name = "mavenCentral"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            credentials {
                username = "SONATYPE_USERNAME".byProperty
                password = "SONATYPE_PASSWORD".byProperty
            }
        }
    }
    publications.register<MavenPublication>("pluginMaven") {
        groupId = project.group.toString()
        artifactId = project.name
        version = detektPluginVersion
        pom {
            description.set("Gradle plugin to support the compiler plugin for Detekt, the Static code analyzer for Kotlin")
            name.set("detekt")
            url.set("https://detekt.dev")
            licenses {
                license {
                    name.set("The Apache Software License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("repo")
                }
            }
            developers {
                developer {
                    id.set("Detekt Developers")
                    name.set("Detekt Developers")
                    email.set("info@detekt.dev")
                }
            }
            scm {
                url.set("https://github.com/detekt/detekt")
            }
        }
    }
}

val signingKey = "SIGNING_KEY".byProperty
val signingPwd = "SIGNING_PWD".byProperty
if (signingKey.isNullOrBlank() || signingPwd.isNullOrBlank()) {
    logger.info("Signing disabled as the GPG key was not found")
} else {
    logger.info("GPG Key found - Signing enabled")
}

signing {
    useInMemoryPgpKeys(signingKey, signingPwd)
    sign(publishing.publications)
    isRequired = !(signingKey.isNullOrBlank() || signingPwd.isNullOrBlank())
}

val String.byProperty: String? get() = providers.gradleProperty(this).orNull
