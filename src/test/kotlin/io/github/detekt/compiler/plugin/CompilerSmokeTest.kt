package io.github.detekt.compiler.plugin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CompilerSmokeTest {

    @Test
    fun `run detekt successfully and find one issue`() {
        val kotlinSource = SourceFile.kotlin(
            "KClass.kt",
            """
                class KClass {
                    fun foo() {
                        val x = 3
                        println(x)
                    }
                }
            """.trimIndent()
        )

        val result = KotlinCompilation().apply {
            sources = listOf(kotlinSource)
            compilerPlugins = listOf(DetektComponentRegistrar())
            commandLineProcessors = listOf(DetektCommandLineProcessor())
        }.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        assertThat(result.messages).contains("MagicNumber")
        assertThat(result.messages).contains("Success?: false")
    }
}
