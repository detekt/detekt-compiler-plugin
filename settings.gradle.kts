rootProject.name = "detekt-compiler-plugin-composite-build"

enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("plugin-build/gradle/libs.versions.toml"))
        }
    }
}

includeBuild("plugin-build")