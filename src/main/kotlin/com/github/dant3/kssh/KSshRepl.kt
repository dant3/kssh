package com.github.dant3.kssh

import com.github.dant3.kssh.repl.Repl
import com.github.dant3.kssh.repl.run
import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import java.io.PrintStream
import java.nio.file.Path
import java.nio.file.Paths

class KSshRepl(sshConfig: SshServerConfig) {
    private val sshd by lazy {
        SshServerBuilder.build(
                sshConfig,
                shellServer = runRepl(
                        Paths.get("~"),
                        "",
                        false,
                        Paths.get("~"),
                        KSshRepl::class.java.classLoader
                ) {
                    stopImmediately()
                }
        )
    }

    val port get() = sshd.port
    fun start(): Unit = sshd.start()
    fun stop(): Unit = sshd.stop()
    fun stopImmediately(): Unit = sshd.stop(true)
    
    companion object {
        // Actually runs a repl inside of session serving a remote user shell.
        private fun runRepl(homePath: Path,
                            predefCode: String,
                            defaultPredef: Boolean,
                            wd: Path,
                            replServerClassLoader: ClassLoader,
                            disposeRepl: () -> Unit): ShellSessionServer = { inputStream, outputStream ->
            // since sshd server has it's own customised environment,
            // where things like System.out will output to the
            // server's console, we need to prepare individual environment
            // to serve this particular user's session

            println("I am alove! We are running repl!!!")

            withEnvironment(Environment(replServerClassLoader, inputStream, outputStream)) {
                try {
                    Repl(Disposable { disposeRepl() }, "kssh", inputStream, outputStream).run()
                    println("Repl is running?!")
                } catch (any: Throwable) {
                    val sshClientOutput = PrintStream(outputStream)
                    sshClientOutput.println("What a terrible failure, the REPL just blow up!")
                    any.printStackTrace(sshClientOutput)

                    println("What a terrible failure!")
                    any.printStackTrace()
                }
            }
        }
    }
}