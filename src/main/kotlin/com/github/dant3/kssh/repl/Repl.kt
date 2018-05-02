package com.github.dant3.kssh.repl

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.GroupingMessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.repl.ReplFromTerminal
import org.jetbrains.kotlin.cli.jvm.repl.configuration.ReplConfiguration
import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.config.CompilerConfiguration
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream


object Repl {
    operator fun invoke(appName: String, inputStream: InputStream, outputStream: OutputStream): ReplFromTerminal
            = invoke(appName, inputStream, PrintStream(outputStream))
    operator fun invoke(disposable: Disposable, appName: String, inputStream: InputStream, outputStream: OutputStream): ReplFromTerminal
            = invoke(disposable, appName, inputStream, PrintStream(outputStream))
    operator fun invoke(appName: String, inputStream: InputStream, outputStream: PrintStream, disposable: Disposable = Disposer.newDisposable(appName)): ReplFromTerminal {
        return ReplFromTerminal(
                disposable,
                compilerConfig(outputStream),
                SshReplConfiguration(appName, inputStream, outputStream)
        )
    }

    private fun compilerConfig(errorStream: PrintStream): CompilerConfiguration = CompilerConfiguration().apply {
        val messageRenderer = MessageRenderer.WITHOUT_PATHS
        val collector = PrintingMessageCollector(errorStream, messageRenderer, true)
        val groupingCollector = GroupingMessageCollector(collector, false)

        put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, groupingCollector);
    }
}

internal fun ReplFromTerminal.doRun() {
    // hack to invoke doRun on our custom repl
    val method = javaClass.getDeclaredMethod("doRun")
    method.isAccessible = true
    method.invoke(this)
}

internal val ReplFromTerminal.replConfiguration: ReplConfiguration
    get() {
        val field = javaClass.getDeclaredField("replConfiguration")
        field.isAccessible = true
        return field.get(this) as ReplConfiguration
    }

fun ReplFromTerminal.run() {
    try {
        doRun()
    } catch (e: Exception) {
        replConfiguration.exceptionReporter.report(e)
        throw e
    }
}