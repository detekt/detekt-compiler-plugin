package io.github.detekt.internal

import io.gitlab.arturbosch.detekt.api.Detektion
import io.gitlab.arturbosch.detekt.core.DetektResult
import io.gitlab.arturbosch.detekt.core.Detektor
import io.gitlab.arturbosch.detekt.core.FileProcessorLocator
import io.gitlab.arturbosch.detekt.core.ProcessingSettings
import io.gitlab.arturbosch.detekt.core.RuleSetLocator
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingContext

class DetektService(
    private val log: MessageCollector,
    private val settings: ProcessingSettings
) {

    fun analyze(files: Collection<KtFile>, context: BindingContext): Detektion {
        val providers = RuleSetLocator(settings).load()
        log.info("Loaded rule sets: ${providers.joinToString { it.ruleSetId }}")
        val processors = FileProcessorLocator(settings).load()
        val engine = Detektor(settings, providers, processors)

        processors.forEach { it.onStart(files.toList()) }
        val rawResult = engine.run(files, context)
        val result = DetektResult(rawResult.toSortedMap())
        processors.forEach { it.onFinish(files.toList(), result) }
        log.info("${files.size} files analyzed")
        return result
    }
}
