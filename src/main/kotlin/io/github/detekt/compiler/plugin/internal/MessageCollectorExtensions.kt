package io.github.detekt.compiler.plugin.internal

import io.gitlab.arturbosch.detekt.api.Detektion
import io.gitlab.arturbosch.detekt.api.Finding
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageUtil

fun MessageCollector.info(msg: String) = this.report(CompilerMessageSeverity.INFO, msg)

fun MessageCollector.warn(msg: String) = this.report(CompilerMessageSeverity.WARNING, msg)

fun MessageCollector.error(msg: String) = this.report(CompilerMessageSeverity.ERROR, msg)

fun MessageCollector.reportFindings(result: Detektion) {
    for ((ruleSetId, findings) in result.findings.entries) {
        if (findings.isNotEmpty()) {
            info("$ruleSetId: ${findings.size} findings found.")
            for (issue in findings) {
                warn(issue.renderAsCompilerWarningMessage())
            }
        }
    }
}

fun Finding.renderAsCompilerWarningMessage(): String {
    val (line, column) = entity.location.source
    val location = MessageUtil.psiElementToMessageLocation(entity.ktElement)
    return "${location?.path ?: entity.location.file}: ($line, $column): ${messageOrDescription()}"
}
