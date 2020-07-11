package io.github.detekt.internal

import io.github.detekt.tooling.api.DetektProvider
import io.github.detekt.tooling.api.InvalidConfig
import io.github.detekt.tooling.api.MaxIssuesReached
import io.github.detekt.tooling.api.UnexpectedError
import io.github.detekt.tooling.api.spec.ProcessingSpec
import io.gitlab.arturbosch.detekt.api.UnstableApi
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingContext

class DetektService(private val log: MessageCollector) {

    @OptIn(UnstableApi::class)
    fun analyze(files: Collection<KtFile>, context: BindingContext) {
        val spec = ProcessingSpec { }
        val detekt = DetektProvider.load().get(spec)
        val result = detekt.run(files, context)
        log.info("${files.size} files analyzed")
        result.container?.let { log.reportFindings(it) }
        when (val error = result.error) {
            is UnexpectedError -> throw error
            is MaxIssuesReached -> Unit // handle based on config
            is InvalidConfig -> Unit // handle based on config
        }
    }
}
