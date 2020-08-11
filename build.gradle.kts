import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val detektVersion: String by project
val kotlinVersion: String by project
val detektPluginVersion: String by project

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
}

detekt {
    debug = true
    isEnabled = System.getProperty("selfAnalysis") != null
    buildUponDefaultConfig = true
}

repositories {
    jcenter()
    mavenLocal()
    maven { setUrl("https://dl.bintray.com/arturbosch/code-analysis") }
}

dependencies {
    compileOnly(gradleApi())
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin-api:$kotlinVersion")
    compileOnly(kotlin("stdlib", kotlinVersion))
    compileOnly(kotlin("compiler-embeddable", kotlinVersion))
    implementation("io.gitlab.arturbosch.detekt:detekt-api:$detektVersion")
    implementation("io.gitlab.arturbosch.detekt:detekt-tooling:$detektVersion")
    runtimeOnly("io.gitlab.arturbosch.detekt:detekt-core:$detektVersion")
    runtimeOnly("io.gitlab.arturbosch.detekt:detekt-rules:$detektVersion")
    runtimeOnly("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf(
        "-Xopt-in=kotlin.RequiresOptIn"
    )
}

val sourcesJar by tasks.creating(Jar::class) {
    dependsOn(tasks.classes)
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.creating(Jar::class) {
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
