package io.github.detekt.compiler.plugin

import io.github.detekt.compiler.plugin.internal.DetektService
import io.github.detekt.compiler.plugin.internal.info
import io.github.detekt.tooling.api.spec.ProcessingSpec
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisHandlerExtension

class DetektAnalysisExtension(
    private val log: MessageCollector,
    private val spec: ProcessingSpec
) : AnalysisHandlerExtension {

    override fun analysisCompleted(
        project: Project,
        module: ModuleDescriptor,
        bindingTrace: BindingTrace,
        files: Collection<KtFile>
    ): AnalysisResult? {
        if (spec.loggingSpec.debug) {
            log.info("$spec")
        }
        log.info("Running detekt on module '${module.name.asString()}'")
        DetektService(log, spec).analyze(files, bindingTrace.bindingContext)
        return null
    }
}
