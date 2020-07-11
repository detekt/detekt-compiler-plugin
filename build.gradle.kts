import com.jfrog.bintray.gradle.BintrayExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jfrog.gradle.plugin.artifactory.dsl.ArtifactoryPluginConvention
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import java.util.Date

buildscript {
    repositories {
        maven { setUrl("https://dl.bintray.com/arturbosch/code-analysis") }
        mavenLocal()
    }
    dependencies {
        if (System.getProperty("skipAnalysis")?.toBoolean() != true) {
            classpath("io.github.detekt:detekt-compiler-plugin:0.2.0")
        }
    }
}

plugins {
    kotlin("jvm")
    id("com.github.ben-manes.versions")
    `maven-publish`
    id("com.jfrog.artifactory")
    id("com.jfrog.bintray")
}

if (System.getProperty("skipAnalysis")?.toBoolean() != true) {
    apply(plugin = "detekt-compiler-plugin")
}

val detektVersion: String by project
val kotlinVersion: String by project
val detektPluginVersion: String by project

group = "io.github.detekt"
version = detektPluginVersion + if (System.getProperty("snapshot")?.toBoolean() == true) "-SNAPSHOT" else ""

repositories {
    jcenter()
    mavenLocal()
}

dependencies {
    compileOnly(gradleApi())
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin-api:$kotlinVersion")
    compileOnly(kotlin("stdlib", version = kotlinVersion))
    compileOnly(kotlin("compiler-embeddable", version = kotlinVersion))
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

val bintrayUser = findProperty("bintrayUser")?.toString() ?: System.getenv("BINTRAY_USER")
val bintrayKey = findProperty("bintrayKey")?.toString() ?: System.getenv("BINTRAY_API_KEY")
val detektPublication = "DetektPublication"

bintray {
    user = bintrayUser
    key = bintrayKey
    val mavenCentralUser = System.getenv("MAVEN_CENTRAL_USER") ?: ""
    val mavenCentralPassword = System.getenv("MAVEN_CENTRAL_PW") ?: ""

    setPublications(detektPublication)

    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "code-analysis"
        name = "detekt-compiler-plugin"
        userOrg = "arturbosch"
        setLicenses("Apache-2.0")
        vcsUrl = "https://github.com/detekt/detekt-compiler-plugin"

        version(delegateClosureOf<BintrayExtension.VersionConfig> {
            name = project.version as? String
            released = Date().toString()

            gpg(delegateClosureOf<BintrayExtension.GpgConfig> {
                sign = true
            })

//            mavenCentralSync(delegateClosureOf<BintrayExtension.MavenCentralSyncConfig> {
//                sync = true
//                user = mavenCentralUser
//                password = mavenCentralPassword
//                close = "1"
//            })
        })
    })
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
    publications.create<MavenPublication>(detektPublication) {
        from(components["java"])
        artifact(sourcesJar)
        artifact(javadocJar)
        groupId = rootProject.group as? String
        artifactId = rootProject.name
        version = rootProject.version as? String
        pom {
            description.set("Static code analysis for Kotlin")
            name.set("detekt")
            url.set("https://arturbosch.github.io/detekt")
            licenses {
                license {
                    name.set("The Apache Software License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("repo")
                }
            }
            developers {
                developer {
                    id.set("Artur Bosch")
                    name.set("Artur Bosch")
                    email.set("arturbosch@gmx.de")
                }
            }
            scm {
                url.set("https://github.com/arturbosch/detekt")
            }
        }
    }
}

configure<ArtifactoryPluginConvention> {
    setContextUrl("https://oss.jfrog.org/artifactory")
    publish(delegateClosureOf<PublisherConfig> {
        repository(delegateClosureOf<groovy.lang.GroovyObject> {
            setProperty("repoKey", "oss-snapshot-local")
            setProperty("username", bintrayUser)
            setProperty("password", bintrayKey)
            setProperty("maven", true)
        })
        defaults(delegateClosureOf<groovy.lang.GroovyObject> {
            invokeMethod("publications", detektPublication)
            setProperty("publishArtifacts", true)
            setProperty("publishPom", true)
        })
    })
}
