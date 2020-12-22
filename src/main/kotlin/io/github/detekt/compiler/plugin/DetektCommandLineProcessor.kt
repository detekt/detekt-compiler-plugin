package io.github.detekt.compiler.plugin

import io.github.detekt.gradle.DETEKT_COMPILER_PLUGIN
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.nio.file.Paths
import java.util.Base64

class DetektCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = DETEKT_COMPILER_PLUGIN

    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        CliOption(
            Options.config,
            "<path|paths>",
            "Comma separated paths to detekt config files.",
            false
        ),
        CliOption(
            Options.configDigest,
            "<digest>",
            "A digest calculated from the content of the config files. Used for Gradle incremental task invalidation.",
            false
        ),
        CliOption(
            Options.baseline,
            "<path>",
            "Path to a detekt baseline file.",
            false
        ),
        CliOption(
            Options.debug,
            "<true|false>",
            "Print debug messages.",
            false
        ),
        CliOption(
            Options.isEnabled,
            "<true|false>",
            "Should detekt run?",
            false
        ),
        CliOption(
            Options.useDefaultConfig,
            "<true|false>",
            "Use the default detekt config as baseline.",
            false
        ),
        CliOption(
            Options.rootPath,
            "<path>",
            "Root path used to relativize paths when using exclude patterns.",
            false
        ),
        CliOption(
            Options.excludes,
            "<base64-encoded globs>",
            "A base64-encoded list of the globs used to exclude paths from scanning.",
            false
        )
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        when (option.optionName) {
            Options.baseline -> configuration.put(Keys.BASELINE, value)
            Options.config -> configuration.put(Keys.CONFIG, value)
            Options.debug -> configuration.put(Keys.DEBUG, value.toBoolean())
            Options.isEnabled -> configuration.put(Keys.IS_ENABLED, value.toBoolean())
            Options.useDefaultConfig -> configuration.put(Keys.USE_DEFAULT_CONFIG, value)
            Options.rootPath -> configuration.put(Keys.ROOT_PATH, Paths.get(value))
            Options.excludes -> configuration.put(Keys.EXCLUDES, value.decodeToGlobSet())
        }
    }
}

private fun String.decodeToGlobSet(): Set<String> {
    val b = Base64.getDecoder().decode(this)
    val bi = ByteArrayInputStream(b)

    return ObjectInputStream(bi).use { inputStream ->
        val globs = mutableSetOf<String>()

        repeat(inputStream.readInt()) {
            globs.add(inputStream.readUTF())
        }

        globs
    }
}
