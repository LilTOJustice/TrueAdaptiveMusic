package liltojustice.trueadaptivemusic

class Logger {
    companion object {
        fun log(message: String, logLevel: LogLevel = LogLevel.INFO) {
            println("TrueAdaptiveMusic [$logLevel] - $message")
        }
    }
}