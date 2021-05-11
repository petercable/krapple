// SEALED CLASSES ARE FUN
sealed class Command {
    companion object {
        fun fromTokens(tokens: List<String>): Command {
            if (tokens.isEmpty()) {
                return Invalid("Must supply command")
            }
            val c = tokens[0].lowercase()
            return when {
                c == "read" && tokens.size == 2 -> Read(tokens[1])
                c == "read" -> Invalid("READ requires exactly one argument")
                c == "write" && tokens.size == 3 -> Write(tokens[1], tokens[2])
                c == "write" -> Invalid("WRITE requires exactly two arguments")
                c == "delete" && tokens.size == 2 -> Delete(tokens[1])
                c == "delete" -> Invalid("DELETE requires exactly one argument")
                c == "start" && tokens.size == 1 -> Start
                c == "start" -> Invalid("START takes no arguments")
                c == "commit" && tokens.size == 1 -> Commit
                c == "commit" -> Invalid("COMMIT takes no arguments")
                c == "abort" && tokens.size == 1 -> Abort
                c == "abort" -> Invalid("ABORT takes no arguments")
                // let's be super flexible with QUIT
                c == "quit" -> Quit
                c == "" -> Continue
                else -> Invalid("${tokens[0]} is not a valid command")
            }
        }
    }
}

data class Read(val key: String): Command()
data class Write(val key: String, val value: String): Command()
data class Delete(val key: String): Command()
object Start : Command()
object Commit : Command()
object Abort : Command()
object Quit : Command()
object Continue: Command()
data class Invalid(val err: String) : Command()