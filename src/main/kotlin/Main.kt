import com.github.dant3.kssh.KSshRepl
import com.github.dant3.kssh.SshServerConfig
import com.github.dant3.kssh.repl.Repl
import com.github.dant3.kssh.repl.run
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import java.nio.file.Paths

object Main {
    @JvmStatic fun main(args: Array<String>) {
        runSshRepl()
    }

    private fun runSshRepl() {
        KSshRepl(
                SshServerConfig("0.0.0.0",
                        10080,
                        Paths.get("/Users/dant3/KSSH.key"),
                        passwordAuthenticator = PasswordAuthenticator { username, password, session ->
                            when {
                                username == "dant3" && password == "123" -> true
                                else -> false
                            }
                        }
                )
        ).start()

        while (true) {
            Thread.sleep(1000)
        }
    }

    private fun runLocalRepl() {
        Repl("local", System.`in`, System.out).run()
    }
}