import com.gradle.enterprise.gradleplugin.internal.extension.BuildScanExtensionWithHiddenFeatures

rootProject.name = "detekt-compiler-plugin-composite-build"

includeBuild("plugin-build")
includeBuild("detekt-compiler-plugin-gradle-plugin")

plugins {
    id("com.gradle.enterprise") version "3.11.1"
    id("com.gradle.common-custom-user-data-gradle-plugin") version "1.7.2"
}

val isCiBuild = System.getenv("CI") != null

gradleEnterprise {
    buildScan {
        publishAlways()

        // Publish to scans.gradle.com when `--scan` is used explicitly
        if (!gradle.startParameter.isBuildScan) {
            server = "https://ge.detekt.dev"
            this as BuildScanExtensionWithHiddenFeatures
            publishIfAuthenticated()
        }

        isUploadInBackground = !isCiBuild

        capture {
            isTaskInputFiles = true
        }
    }
}
