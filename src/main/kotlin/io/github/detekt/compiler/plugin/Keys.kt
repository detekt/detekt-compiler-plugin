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
    const val report = "report"
}

object Keys {

    val DEBUG = CompilerConfigurationKey.create<Boolean>(Options.debug)
    val IS_ENABLED = CompilerConfigurationKey.create<Boolean>(Options.isEnabled)
    val CONFIG = CompilerConfigurationKey.create<List<Path>>(Options.config)
    val BASELINE = CompilerConfigurationKey.create<Path>(Options.baseline)
    val USE_DEFAULT_CONFIG = CompilerConfigurationKey.create<Boolean>(Options.useDefaultConfig)
    val ROOT_PATH = CompilerConfigurationKey.create<Path>(Options.rootPath)
    val EXCLUDES = CompilerConfigurationKey.create<List<String>>(Options.excludes)
    val REPORTS = CompilerConfigurationKey.create<Map<String, Path>>(Options.report)
}
