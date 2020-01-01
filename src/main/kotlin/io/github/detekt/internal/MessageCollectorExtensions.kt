package io.github.detekt.internal

import io.gitlab.arturbosch.detekt.api.Detektion
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

fun MessageCollector.info(msg: String) = this.report(CompilerMessageSeverity.INFO, msg)

fun MessageCollector.warn(msg: String) = this.report(CompilerMessageSeverity.WARNING, msg)

fun MessageCollector.reportFindings(result: Detektion) {
    info("${result.findings.values.sumBy { it.size }} findings found.")

    for ((ruleSetId, findings) in result.findings.entries) {
        if (findings.isNotEmpty()) {
            warn(ruleSetId)
            for (issue in findings) {
                warn("\t${issue.compact()}")
            }
        }
    }
}
