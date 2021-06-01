import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.Verify
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

val kotlinVersion: String by project
val kotlinCompilerChecksum: String by project
val detektPluginVersion: String by project

group = "io.github.detekt"
version = detektPluginVersion

val detektPublication = "DetektPublication"

plugins {
    kotlin("jvm") version "1.4.32"
    id("maven-publish")
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "0.14.0"
    id("com.github.ben-manes.versions") version "0.38.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("de.undercouch.download") version "4.1.1"
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(kotlin("gradle-plugin"))
    compileOnly(kotlin("gradle-plugin-api"))
    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("compiler-embeddable"))

    implementation(libs.detekt.api)
    implementation(libs.detekt.tooling)
    runtimeOnly(libs.detekt.core)
    runtimeOnly(libs.detekt.rules)

    testImplementation(libs.assertj.core)
    testImplementation(libs.kotlinCompileTesting)
    testImplementation(libs.spek.dsl)
    testRuntimeOnly(libs.spek.runner)
}

val javaComponent = components["java"] as AdhocComponentWithVariants
javaComponent.withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) {
    skip()
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

tasks.withType<Test>().configureEach {
    useJUnitPlatform {
        includeEngines("spek2")
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
