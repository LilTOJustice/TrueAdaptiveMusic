package liltojustice.trueadaptivemusic

class Logger {
    companion object {
        fun log(message: String, logLevel: LogLevel = LogLevel.INFO) {
            val logger = TrueAdaptiveMusic.LOGGER
            when(logLevel) {
                LogLevel.INFO -> logger.info(message)
                LogLevel.WARNING -> logger.warn(message)
                LogLevel.ERROR -> logger.error(message)
            }
        }
    }
}