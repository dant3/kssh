package com.github.dant3.kssh.repl

import org.jetbrains.kotlin.cli.jvm.repl.ReplExceptionReporter
import org.jetbrains.kotlin.cli.jvm.repl.configuration.ReplConfiguration
import org.jetbrains.kotlin.cli.jvm.repl.configuration.SnippetExecutionInterceptor
import org.jetbrains.kotlin.cli.jvm.repl.messages.ConsoleDiagnosticMessageHolder
import org.jetbrains.kotlin.cli.jvm.repl.messages.DiagnosticMessageHolder
import org.jetbrains.kotlin.cli.jvm.repl.reader.ReplCommandReader
import org.jetbrains.kotlin.cli.jvm.repl.writer.ReplWriter
import java.io.InputStream
import java.io.PrintStream

class SshReplConfiguration(appName: String, inputStream: InputStream, outputStream: PrintStream): ReplConfiguration {
    override val allowIncompleteLines: Boolean = true
    override val commandReader: ReplCommandReader = SshReplCommandReader(appName, inputStream, outputStream)
    override val exceptionReporter: ReplExceptionReporter = ReplExceptionReporter
    override val executionInterceptor: SnippetExecutionInterceptor = SnippetExecutionInterceptor

    override val writer: ReplWriter = PrinterReplWriter(outputStream)
    override fun createDiagnosticHolder(): DiagnosticMessageHolder = ConsoleDiagnosticMessageHolder()
}