package io.github.detekt

import io.github.detekt.internal.DetektService
import io.github.detekt.internal.info
import io.github.detekt.internal.reportFindings
import io.gitlab.arturbosch.detekt.api.internal.ABSOLUTE_PATH
import io.gitlab.arturbosch.detekt.cli.loadDefaultConfig
import io.gitlab.arturbosch.detekt.core.ProcessingSettings
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisHandlerExtension

class DetektComponentRegistrar : ComponentRegistrar {

    override fun registerProjectComponents(
        project: MockProject,
        configuration: CompilerConfiguration
    ) {
        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY)
            ?: MessageCollector.NONE
        val extension = DetektExtension(messageCollector)
        AnalysisHandlerExtension.registerExtension(project, extension)
    }
}

class DetektExtension(private val log: MessageCollector) : AnalysisHandlerExtension {

    override fun analysisCompleted(
        project: Project,
        module: ModuleDescriptor,
        bindingTrace: BindingTrace,
        files: Collection<KtFile>
    ): AnalysisResult? {
        log.info("Starting detekt")
        prepareFiles(files)
        val service = DetektService(log, createDefaultSettings())
        val result = service.analyze(files, bindingTrace.bindingContext)
        log.reportFindings(result)
        return null
    }

    private fun prepareFiles(files: Collection<KtFile>) {
        files.forEach { it.putUserData(ABSOLUTE_PATH, it.containingKtFile.virtualFilePath) }
    }

    private fun createDefaultSettings() = ProcessingSettings(
        emptyList(),
        config = loadDefaultConfig(),
        parallelCompilation = false
    )
}
