package io.github.detekt

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor

class DetektCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = "detekt-compiler-plugin"
    override val pluginOptions: Collection<AbstractCliOption> = emptyList()
}
