package com.github.dant3.kssh

import org.apache.sshd.server.Command
import org.apache.sshd.server.Environment
import org.apache.sshd.server.ExitCallback
import java.io.InputStream
import java.io.OutputStream


/**
 * Implementation of ssh server's remote shell session,
 * which will be serving remote user.
 * @param remoteShell actual shell implementation,
 *                    which will serve remote user's shell session.
 */

internal class ShellSession(private val remoteShell: ShellSessionServer): Command {
    private lateinit var input: InputStream
    private lateinit var output: OutputStream
    private var exit: ExitCallback? = null

    private val thread by lazy {
        createShellServingThread()
    }

    override fun setInputStream(input: InputStream) {
        this.input = input
    }

    override fun setOutputStream(output: OutputStream) {
        this.output = SshOutputStream(output)
    }

    /* ammonite doesn't uses err stream so we don't need this */
    override fun setErrorStream(err: OutputStream) {}

    /**
     * called by ssh server to instrument this session
     * with a callback that it finished serving a user
     */
    override fun setExitCallback(exit: ExitCallback) {
        this.exit = exit
    }

    /**
     * called when ssh server is ready to start this session.
     * Starts the actual shell-serving task.
     */
    override fun start(env: Environment) {
        thread.start()
    }

    /**
     * called when ssh server wants to destroy shell session.
     * Whatever shell session serving a user was doing at this moment
     * we are free to stop it.
     */
    override fun destroy() {
        thread.interrupt()
    }


    private fun createShellServingThread(): Thread = object: Thread() {
        override fun run() {
            remoteShell(input, output)
            exit?.onExit(0, "repl finished")
        }
    }


    // proxy which fixes output to the remote side to be ssh compatible.
    private class SshOutputStream(private val out: OutputStream) : OutputStream() {
        override fun close() {
            out.close()
        }

        override fun flush() {
            out.flush()
        }

        fun write(byte: Byte) {
            write(byte.toInt())
        }

        override fun write(byte: Int): Unit {
            // ssh client's only accepts new lines with \r so we make \n to be \r\n.
            // Unneeded \r will not be seen anyway
            if (byte.toChar() == '\n') out.write('\r'.toInt())
            out.write(byte)
        }

        override fun write(bytes: ByteArray): Unit {
            bytes.forEach { write(it) }
        }

        override fun write(bytes: ByteArray, offset: Int, length: Int): Unit {
            write(bytes.sliceArray(offset .. offset + length))
        }
    }
}

typealias ShellSessionServer = ((InputStream, OutputStream) -> Unit)