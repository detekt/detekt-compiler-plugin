package io.github.detekt.compiler.plugin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object CompilerTest : Spek({

    describe("smoke test") {

        val kotlinSource by memoized {
            SourceFile.kotlin("KClass.kt", """
                class KClass {
                    fun foo() {
                        val x = 3
                        println(x)
                    }
                }
            """)
        }

        it("will successfully run detekt and find one issue") {
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
})
