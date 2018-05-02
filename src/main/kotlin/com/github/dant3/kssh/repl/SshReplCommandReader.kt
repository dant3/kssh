package com.github.dant3.kssh.repl

import org.jetbrains.kotlin.cli.jvm.repl.ReplFromTerminal
import org.jetbrains.kotlin.cli.jvm.repl.reader.ReplCommandReader
import org.jetbrains.kotlin.org.jline.reader.EndOfFileException
import org.jetbrains.kotlin.org.jline.reader.LineReader
import org.jetbrains.kotlin.org.jline.reader.LineReaderBuilder
import org.jetbrains.kotlin.org.jline.reader.UserInterruptException
import org.jetbrains.kotlin.org.jline.terminal.Terminal
import org.jetbrains.kotlin.org.jline.terminal.impl.ExternalTerminal
import java.io.InputStream
import java.io.OutputStream
import java.util.logging.Level
import java.util.logging.Logger

class SshReplCommandReader(appName: String, input: InputStream, output: OutputStream): ReplCommandReader {
    private val lineReader = LineReaderBuilder.builder()
            .appName(appName)
            .terminal(ExternalTerminal("Terminal", "SSH", input, output, "UTF-8", Terminal.SignalHandler.SIG_DFL))
            //.variable(LineReader.HISTORY_FILE, File(File(System.getProperty("user.home")), ".kotlinc_history").absolutePath)
            .build()
            .apply {
                setOpt(LineReader.Option.DISABLE_EVENT_EXPANSION)
            }

    override fun readLine(next: ReplFromTerminal.WhatNextAfterOneLine): String? {
        val prompt = if (next == ReplFromTerminal.WhatNextAfterOneLine.INCOMPLETE) "... " else ">>> "
        return try {
            lineReader.readLine(prompt)
        } catch (e: UserInterruptException) {
            println("<interrupted>")
            System.out.flush()
            ""
        } catch (e: EndOfFileException) {
            null
        }
    }

    override fun flushHistory() = lineReader.history.save()

    private companion object {
        init {
            Logger.getLogger("org.jline").level = Level.OFF;
        }
    }
}