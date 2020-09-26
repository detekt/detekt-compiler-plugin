import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.Verify
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

val assertJVersion: String by project
val detektVersion: String by project
val kotlinVersion: String by project
val kotlinCompilerChecksum: String by project
val kotlinCompileTestVersion: String by project
val detektPluginVersion: String by project
val junitVersion: String by extra

group = "io.github.detekt"
version = detektPluginVersion

val bintrayUser: String? = findProperty("bintrayUser")?.toString() ?: System.getenv("BINTRAY_USER")
val bintrayKey: String? = findProperty("bintrayKey")?.toString() ?: System.getenv("BINTRAY_API_KEY")
val detektPublication = "DetektPublication"

plugins {
    kotlin("jvm")
    id("com.github.ben-manes.versions")
    `maven-publish`
    `java-gradle-plugin`
    id("com.gradle.plugin-publish")
    id("io.github.detekt.gradle.compiler-plugin")
    id("com.github.johnrengelman.shadow")
}

detekt {
    debug = true
    isEnabled = System.getProperty("selfAnalysis") != null
}

repositories {
    mavenCentral()
    mavenLocal()
    maven { setUrl("https://dl.bintray.com/arturbosch/code-analysis") }
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(kotlin("gradle-plugin-api", kotlinVersion))
    compileOnly(kotlin("stdlib", kotlinVersion))
    compileOnly(kotlin("compiler-embeddable", kotlinVersion))
    implementation("io.gitlab.arturbosch.detekt:detekt-api:$detektVersion")
    implementation("io.gitlab.arturbosch.detekt:detekt-tooling:$detektVersion")
    runtimeOnly("io.gitlab.arturbosch.detekt:detekt-core:$detektVersion")
    runtimeOnly("io.gitlab.arturbosch.detekt:detekt-rules:$detektVersion")
    runtimeOnly("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")
    testImplementation("org.assertj:assertj-core:$assertJVersion")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:$kotlinCompileTestVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events = setOf(
            TestLogEvent.FAILED,
            TestLogEvent.STANDARD_ERROR,
            TestLogEvent.STANDARD_OUT,
            TestLogEvent.SKIPPED
        )
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

tasks.shadowJar.configure {
    relocate("org.jetbrains.kotlin.com.intellij", "com.intellij")
    mergeServiceFiles()
    dependencies {
        exclude(dependency("org.jetbrains.intellij.deps:trove4j"))
        exclude(dependency("org.jetbrains:annotations"))
        exclude(dependency("org.jetbrains.kotlin:kotlin-compiler-embeddable"))
        exclude(dependency("org.jetbrains.kotlin:kotlin-daemon-embeddable"))
        exclude(dependency("org.jetbrains.kotlin:kotlin-reflect"))
        exclude(dependency("org.jetbrains.kotlin:kotlin-script-runtime"))
        exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
        exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib-common"))
    }
}

val verifyKotlinCompilerDownload by tasks.registering(Verify::class) {
    src(file("$rootDir/.kotlinc/kotlin-compiler-$kotlinVersion.zip"))
    algorithm("SHA-256")
    checksum(kotlinCompilerChecksum)
    outputs.upToDateWhen { true }
}

val downloadKotlinCompiler by tasks.registering(Download::class) {
    src("https://github.com/JetBrains/kotlin/releases/download/v$kotlinVersion/kotlin-compiler-$kotlinVersion.zip")
    dest(file("$rootDir/.kotlinc/kotlin-compiler-$kotlinVersion.zip"))
    overwrite(false)
    finalizedBy(verifyKotlinCompilerDownload)
}

val unzipKotlinCompiler by tasks.registering(Copy::class) {
    dependsOn(downloadKotlinCompiler)
    from(zipTree(downloadKotlinCompiler.get().dest))
    into(file("$rootDir/.kotlinc/$kotlinVersion"))
}

val testPluginKotlinc by tasks.registering(RunTestExecutable::class) {
    dependsOn(unzipKotlinCompiler, tasks.shadowJar)
    executable(file("${unzipKotlinCompiler.get().destinationDir}/kotlinc/bin/kotlinc"))
    args(
        listOf(
            "$rootDir/src/test/resources/hello.kt",
            "-Xplugin=${tasks.shadowJar.get().archiveFile.get().asFile.absolutePath}",
            "-P",
            "plugin:detekt-compiler-plugin:debug=true"
        )
    )
    errorOutput = ByteArrayOutputStream()
    // dummy path - required for RunTestExecutable task but doesn't do anything
    outputDir = file("$buildDir/tmp/kotlinc")

    doLast {
        if (!errorOutput.toString().contains("MagicNumber - [x] at hello.kt")) {
            throw GradleException(
                "kotlinc $kotlinVersion run with compiler plugin did not find MagicNumber issue as expected"
            )
        }
        (this as RunTestExecutable).execResult!!.assertNormalExitValue()
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf(
        "-Xopt-in=kotlin.RequiresOptIn"
    )
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

publishing {
    repositories {
        maven {
            name = "bintray"
            url = uri(
                "https://api.bintray.com/maven/arturbosch/code-analysis/detekt-compiler-plugin/" +
                    ";publish=1;override=1"
            )
            credentials {
                username = bintrayUser
                password = bintrayKey
            }
        }
    }
    publications.create<MavenPublication>(detektPublication) {
        from(components["java"])
        artifact(sourcesJar)
        artifact(javadocJar)
        groupId = rootProject.group as? String
        artifactId = rootProject.name
        version = rootProject.version as? String
        pom {
            description.set("Static code analysis for Kotlin as a compiler plugin.")
            name.set("detekt-compiler-plugin")
            url.set("https://detekt.github.io/detekt")
            licenses {
                license {
                    name.set("The Apache Software License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("repo")
                }
            }
            scm {
                url.set("https://github.com/detekt/detekt")
            }
        }
    }
}

gradlePlugin {
    plugins {
        register("detektCompilerPlugin") {
            id = "io.github.detekt.gradle.compiler-plugin"
            implementationClass = "io.github.detekt.gradle.DetektGradlePlugin"
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
