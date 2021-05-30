package io.github.detekt.gradle

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

class DetektKotlinCompilerPluginTest {

    @Test
    fun `it calculates the expected Base64-encoded SHA-256 digest`() {
        val project = ProjectBuilder.builder().build()
        val file1 = javaClass.classLoader.getResource("DetektKotlinCompilerPluginTest/hello.kt")
        val file2 = javaClass.classLoader.getResource("DetektKotlinCompilerPluginTest/hello2.kt")

        val fileCollection = project.files(file1, file2)

        assertThat(fileCollection.toDigest()).isEqualTo("Jm9xCn/w7YEc0RCR2iD6gUbr7BNxejj3Tvp871W/JEY=")
    }
}