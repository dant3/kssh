package com.github.dant3.kssh.repl

import org.jetbrains.kotlin.cli.jvm.repl.writer.ReplWriter
import java.io.PrintStream

class PrinterReplWriter(private val printer: PrintStream): ReplWriter {
    private fun println(x: String) {
        printer.println(x)
    }

    override fun printlnWelcomeMessage(x: String) = println(x)
    override fun printlnHelpMessage(x: String) = println(x)
    override fun outputCompileError(x: String) = println(x)
    override fun outputCommandResult(x: String) = println(x)
    override fun outputRuntimeError(x: String) = println(x)

    override fun notifyReadLineStart() {}
    override fun notifyReadLineEnd() {}
    override fun notifyIncomplete() {}
    override fun notifyCommandSuccess() {}
    override fun sendInternalErrorReport(x: String) {}
}