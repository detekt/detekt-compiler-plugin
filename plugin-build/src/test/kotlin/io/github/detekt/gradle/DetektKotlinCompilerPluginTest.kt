package io.github.detekt.gradle

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.util.Locale

object DetektKotlinCompilerPluginTest: Spek({
    describe("ConfigurableFileCollection.toDigest()") {
        it("calculates the expected Base64-encoded SHA-256 digest") {
            val project = ProjectBuilder.builder().build()

            val file1 = javaClass.classLoader.getResource("DetektKotlinCompilerPluginTest/hello.kt")
            val file2 = javaClass.classLoader.getResource("DetektKotlinCompilerPluginTest/hello2.kt")
            val fileCollection = project.files(file1, file2)

            val expectedDigest = if ("win" in System.getProperty("os.name").toLowerCase(Locale.ROOT)) {
                "4NwcqDfQOdBVnJx6wqUnyL+9Zr4ClzGz1nSlRKaz23Q="
            } else {
                "Jm9xCn/w7YEc0RCR2iD6gUbr7BNxejj3Tvp871W/JEY="
            }

            assertThat(fileCollection.toDigest()).isEqualTo(expectedDigest)
        }
    }
})
