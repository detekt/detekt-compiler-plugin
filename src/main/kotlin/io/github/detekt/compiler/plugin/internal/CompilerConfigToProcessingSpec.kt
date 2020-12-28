package io.github.detekt.compiler.plugin.internal

import io.github.detekt.compiler.plugin.Keys
import io.github.detekt.tooling.api.spec.ProcessingSpec
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CompilerConfiguration
import java.nio.file.Paths

internal fun CompilerConfiguration.toSpec(log: MessageCollector) = ProcessingSpec.invoke {
    config {
        configPaths = get(Keys.CONFIG)?.split(",;")?.map { Paths.get(it) } ?: emptyList()
        useDefaultConfig = get(Keys.USE_DEFAULT_CONFIG)?.toBoolean() ?: false
    }
    baseline {
        path = get(Keys.BASELINE)?.let { Paths.get(it) }
    }
    logging {
        debug = get(Keys.DEBUG) ?: false
        outputChannel = AppendableAdapter { log.info(it) }
        errorChannel = AppendableAdapter { log.error(it) }
    }
    reports {
        get(Keys.REPORTS)?.forEach {
            report { Pair(it.key, it.value) }
        }
    }
}
