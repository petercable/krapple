import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintStream
import kotlin.system.exitProcess

fun main() {
    // buffer the InputStream so we can deal with it line-wise
    val reader = BufferedReader(InputStreamReader(System.`in`))
    Krapple(reader, System.out, System.err).doTheThing()
}

class Krapple(
    private val input: BufferedReader,
    private val out: PrintStream,
    private val err: PrintStream,
) {
    private val store = Store()
    private val whitespace = "\\s+".toRegex()

    fun doTheThing() {
        while (true) {
            prompt()
            val tokens = getTokenz() ?: exitProcess(1)
            kotlin.runCatching {
                when (val command = Command.fromTokens(tokens)) {
                    is Read -> {
                        val value = store.read(command.key)
                        out.println(value)
                    }
                    is Write -> store.write(command.key, command.value)
                    is Delete -> store.delete(command.key)
                    Start -> store.start()
                    Commit -> store.commit()
                    Abort -> store.abort()
                    is Invalid -> error(command.err)
                    is Quit -> {
                        out.println("BYE!!!")
                        exitProcess(0)
                    }
                }
            }.onFailure {
                when (it) {
                    is KeyNotFound -> error("Key not found: ${it.key}")
                    is NoTransaction -> error("No transaction in progress")
                }
            }
        }
    }

    private fun prompt() {
        repeat(store.level) { out.print(">") }
        out.print(" ")
        out.flush()
    }

    private fun error(s: String) = err.println(s)

    private fun getTokenz(): List<String>? = input.readLine()?.trim()?.split(whitespace, 3)
}

