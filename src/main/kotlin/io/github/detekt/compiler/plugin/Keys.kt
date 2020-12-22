package io.github.detekt.compiler.plugin

import org.jetbrains.kotlin.config.CompilerConfigurationKey
import java.nio.file.Path

object Options {

    const val isEnabled: String = "isEnabled"
    const val debug: String = "debug"
    const val config = "config"
    const val configDigest: String = "configDigest"
    const val baseline: String = "baseline"
    const val useDefaultConfig: String = "useDefaultConfig"
    const val rootPath = "rootDir"
    const val excludes = "excludes"
}

object Keys {

    val DEBUG = CompilerConfigurationKey.create<Boolean>(Options.debug)
    val IS_ENABLED = CompilerConfigurationKey.create<Boolean>(Options.isEnabled)
    val CONFIG = CompilerConfigurationKey.create<String>(Options.config)
    val BASELINE = CompilerConfigurationKey.create<String>(Options.baseline)
    val USE_DEFAULT_CONFIG = CompilerConfigurationKey.create<String>(Options.useDefaultConfig)
    val ROOT_PATH = CompilerConfigurationKey.create<Path>(Options.rootPath)
    val EXCLUDES = CompilerConfigurationKey.create<Set<String>>(Options.excludes)
}
