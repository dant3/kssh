package com.github.dant3.kssh

import org.apache.sshd.agent.SshAgentFactory
import org.apache.sshd.common.FactoryManager
import org.apache.sshd.common.file.FileSystemFactory
import org.apache.sshd.common.session.ConnectionService
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.keyboard.DefaultKeyboardInteractiveAuthenticator
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import java.nio.file.Files
import java.nio.file.Path

/**
 * A factory to simplify creation of ssh server
 */
object SshServerBuilder {
    fun build(options: SshServerConfig, shellServer: ShellSessionServer): SshServer {
        val sshServer = SshServer.setUpDefaultServer()
        sshServer.host = options.address
        sshServer.port = options.port
        options.passwordAuthenticator?.let { auth ->
            sshServer.passwordAuthenticator = auth
            sshServer.keyboardInteractiveAuthenticator = DefaultKeyboardInteractiveAuthenticator()
        }
        options.publicKeyAuthenticator?.let { auth ->
            sshServer.publickeyAuthenticator = auth
        }
        sshServer.keyPairProvider = keyPairProvider(options)
        sshServer.setShellFactory { ShellSession(shellServer) }
        return disableUnsupportedChannels(sshServer)
    }

    fun keyPairProvider(options: SshServerConfig): SimpleGeneratorHostKeyProvider {
        val hostKeyFile = touch(options.hostKeyFile ?: fallbackHostkeyFilePath(options))
        val provider = SimpleGeneratorHostKeyProvider(hostKeyFile)
        provider.algorithm = "RSA"
        return provider
    }

    private fun disableUnsupportedChannels(sshServer: SshServer): SshServer {
        // exec can't really be disabled
        // but it can report error on trying to run any command it received
        sshServer.commandFactory = CommandFactory {
            throw IllegalArgumentException("exec is not supported")
        }
        sshServer.subsystemFactories = emptyList()
        sshServer.tcpipForwardingFilter = null
        sshServer.agentFactory = object: SshAgentFactory {
            override fun createServer(service: ConnectionService) = null
            override fun createClient(manager: FactoryManager) = null
            override fun getChannelForwardingFactory() = null
        }
        sshServer.fileSystemFactory = FileSystemFactory { null }
        return sshServer
    }

    // this is a user-safe options.
    // Server should have stable key
    // to not violate the user under threat of MITM attack
    private fun fallbackHostkeyFilePath(options: SshServerConfig): Path = TODO()
        //options.ammoniteHome/'cache/'ssh/'hostkeys

    private fun touch(file: Path): Path {
        if (!Files.exists(file)) {
            mkdirs(file.parent)
            Files.createFile(file)
        }
        return file
    }

    private fun mkdirs(file: Path) {
        if (!Files.isDirectory(file)) {
            Files.deleteIfExists(file)
            Files.createDirectories(file)
        }
    }
}