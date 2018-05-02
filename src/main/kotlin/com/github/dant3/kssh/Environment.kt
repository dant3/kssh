package com.github.dant3.kssh

import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream


/**
 * Container for staging environment important for Ammonite repl to run correctly.
 * @param thread a thread where execution takes place. Important for restoring contextClassLoader
 * @param contextClassLoader thread's context class loader. Ammonite repl uses that to load classes
 * @param systemIn
 * @param systemOut
 * @param systemErr
 */
internal data class Environment(
        val thread: Thread,
        val contextClassLoader: ClassLoader,
        val systemIn: InputStream,
        val systemOut: PrintStream,
        val systemErr: PrintStream
) {
    companion object {
        operator fun invoke(classLoader: ClassLoader, input: InputStream, output: PrintStream): Environment =
                Environment(Thread.currentThread(), classLoader, input, output, output)
        operator fun invoke(classLoader: ClassLoader, input: InputStream, output: OutputStream): Environment =
                invoke(classLoader, input, PrintStream(output))
    }
}

internal fun withEnvironment(env: Environment, code: () -> Any): Any =
        env.thread.withClassLoader(env.contextClassLoader) {
            withSystemIn(env.systemIn) {
                withSystemOut(env.systemOut) {
                    withSystemErr(env.systemErr) {
                        code
                    }
                }
            }
        }

private fun <T> Thread.withClassLoader(classLoader: ClassLoader, block: () -> T): T {
    val oldClassLoader = contextClassLoader
    try {
        contextClassLoader = classLoader
        return block()
    } finally {
        contextClassLoader = oldClassLoader
    }
}

private fun <T> withSystemIn(input: InputStream, block: () -> T): T {
    val oldSystemIn = System.`in`
    try {
        System.setIn(input)
        return block()
    } finally {
        System.setIn(oldSystemIn)
    }
}

private fun <T> withSystemOut(output: PrintStream, block: () -> T): T {
    val oldSystemOut = System.out
    try {
        System.setOut(output)
        return block()
    } finally {
        System.setOut(oldSystemOut)
    }
}

private fun <T> withSystemErr(err: PrintStream, block: () -> T): T {
    val oldSystemErr = System.err
    try {
        System.setErr(err)
        return block()
    } finally {
        System.setErr(oldSystemErr)
    }
}