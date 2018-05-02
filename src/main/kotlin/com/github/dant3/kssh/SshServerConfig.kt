package com.github.dant3.kssh

import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator
import java.nio.file.Path

/**
 * Ssh server parameters
 * @param port a port to be used by ssh server. Set it as `0` to let server choose some random port.
 * @param hostKeyFile path to the place where to store server's identity key
 */
data class SshServerConfig(val address: String,
                           val port: Int,
                           val hostKeyFile: Path? = null,
                           val passwordAuthenticator: PasswordAuthenticator? = null,
                           val publicKeyAuthenticator: PublickeyAuthenticator? = null
) {
    init {
        require(passwordAuthenticator != null || publicKeyAuthenticator != null) {
            "you must provide at least one authenticator"
        }
    }

    override fun toString() =
            "SshServerConfig(address = $address, port = $port, hostKeyFile = $hostKeyFile, " +
                    "passwordAuthenticator = $passwordAuthenticator, " +
                    "publicKeyAuthenticator = $publicKeyAuthenticator)"
}
