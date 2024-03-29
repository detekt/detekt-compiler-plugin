import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.Verify
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

val kotlinVersion: String = libs.versions.kotlin.get()

val kotlinCompilerChecksum: String by project
val detektPluginVersion: String by project

group = "io.github.detekt"
version = detektPluginVersion

val detektPublication = "DetektPublication"

plugins {
    alias(libs.plugins.kotlin)
    id("maven-publish")
    id("signing")
    id("jvm-test-suite")
    alias(libs.plugins.gradleVersionz)
    alias(libs.plugins.shadow)
    alias(libs.plugins.download)
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("compiler-embeddable"))

    implementation(libs.detekt.api)
    implementation(libs.detekt.tooling)
    runtimeOnly(libs.detekt.core)
    runtimeOnly(libs.detekt.rules)

    testImplementation(libs.assertj.core)
    testImplementation(libs.kotlinCompileTesting)
}

val javaComponent = components["java"] as AdhocComponentWithVariants
javaComponent.withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) {
    skip()
}

tasks.shadowJar.configure {
    relocate("org.jetbrains.kotlin.com.intellij", "com.intellij")
    mergeServiceFiles()
    dependencies {
        include(dependency("io.gitlab.arturbosch.detekt:.*"))
        include(dependency("io.github.detekt:.*"))
        include(dependency("org.yaml:snakeyaml"))
        include(dependency("io.github.davidburstrom.contester:contester-breakpoint"))
    }
}

val verifyKotlinCompilerDownload by tasks.registering(Verify::class) {
    src(file("$rootDir/build/kotlinc/kotlin-compiler-$kotlinVersion.zip"))
    algorithm("SHA-256")
    checksum(kotlinCompilerChecksum)
    outputs.upToDateWhen { true }
}

val downloadKotlinCompiler by tasks.registering(Download::class) {
    src("https://github.com/JetBrains/kotlin/releases/download/v$kotlinVersion/kotlin-compiler-$kotlinVersion.zip")
    dest(file("$rootDir/build/kotlinc/kotlin-compiler-$kotlinVersion.zip"))
    overwrite(false)
    finalizedBy(verifyKotlinCompilerDownload)
}

val unzipKotlinCompiler by tasks.registering(Copy::class) {
    dependsOn(downloadKotlinCompiler)
    from(zipTree(downloadKotlinCompiler.get().dest))
    into(file("$rootDir/build/kotlinc/$kotlinVersion"))
}

val testPluginKotlinc by tasks.registering(RunTestExecutable::class) {
    dependsOn(unzipKotlinCompiler, tasks.shadowJar)

    args(
        listOf(
            "$rootDir/src/test/resources/hello.kt",
            "-Xplugin=${tasks.shadowJar.get().archiveFile.get().asFile.absolutePath}",
            "-P",
        )
    )

    val baseExecutablePath = "${unzipKotlinCompiler.get().destinationDir}/kotlinc/bin/kotlinc"
    val pluginParameters = "plugin:detekt-compiler-plugin:debug=true"

    if (org.apache.tools.ant.taskdefs.condition.Os.isFamily("windows")) {
        executable(file("$baseExecutablePath.bat"))
        args("\"$pluginParameters\"")
    } else {
        executable(file(baseExecutablePath))
        args(pluginParameters)
    }

    errorOutput = ByteArrayOutputStream()
    // dummy path - required for RunTestExecutable task but doesn't do anything
    outputDir = file("$buildDir/tmp/kotlinc")

    doLast {
        if (!errorOutput.toString().contains("warning: magicNumber:")) {
            throw GradleException(
                "kotlinc $kotlinVersion run with compiler plugin did not find MagicNumber issue as expected"
            )
        }
        (this as RunTestExecutable).executionResult.get().assertNormalExitValue()
    }
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

java {
    withSourcesJar()
    withJavadocJar()
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
    publications.register<MavenPublication>(detektPublication) {
        groupId = project.group.toString()
        artifactId = project.name
        from(components["java"])
        version = detektPluginVersion
        pom {
            description.set("Compiler plugin for Detekt, the Static code analyzer for Kotlin")
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
