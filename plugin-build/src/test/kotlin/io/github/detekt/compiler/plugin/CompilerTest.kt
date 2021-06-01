package io.github.detekt.compiler.plugin

import io.github.detekt.compiler.plugin.util.CompilerTestUtils.compile
import io.github.detekt.compiler.plugin.util.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object CompilerTest: Spek({
    describe("smoke test") {
        it("with a source file that contains violations") {
            val result = compile(
                """
                class KClass {
                    fun foo() {
                        val x = 3
                        println(x)
                    }
                }
                """
            )

            assertThat(result)
                .passCompilation(true)
                .passDetekt(false)
                .withViolations(2)
                .withRuleViolation("MagicNumber", "NewLineAtEndOfFile")
        }

        it("with a source file that contains local suppression") {
            val result = compile(
                """
                @file:Suppress("NewLineAtEndOfFile")
                class KClass {
                    fun foo() {
                        @Suppress("MagicNumber")
                        val x = 3
                        println(x)
                    }
                }
                """
            )

            assertThat(result)
                .passCompilation()
                .passDetekt()
                .withNoViolations()
        }

        it("with a source file that does not contain violations") {
            val result = compile(
                """
                class KClass {
                    fun foo() {
                        println("Hello world :)")
                    }
                }
                
                """
            )

            assertThat(result)
                .passCompilation()
                .passDetekt()
                .withNoViolations()
        }
    }
})
