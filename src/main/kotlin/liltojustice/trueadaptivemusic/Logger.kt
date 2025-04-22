package liltojustice.trueadaptivemusic

object Logger {
    private val oneTimeLogs = mutableSetOf<String>()
    private fun log(message: String, logLevel: LogLevel = LogLevel.INFO, oneTime: Boolean = false) {
        if (oneTimeLogs.contains(message)) {
            return
        }

        if (oneTime) {
            oneTimeLogs.add(message)
        }

        val logger = TrueAdaptiveMusic.LOGGER
        when(logLevel) {
            LogLevel.INFO -> logger.info(message)
            LogLevel.WARNING -> logger.warn(message)
            LogLevel.ERROR -> logger.error(message)
        }
    }

    fun logInfo(message: String, oneTime: Boolean = false) {
        log(message, LogLevel.INFO, oneTime)
    }

    fun logWarning(message: String, oneTime: Boolean = false) {
        log(message, LogLevel.WARNING, oneTime)
    }

    fun logError(message: String, oneTime: Boolean = false) {
        log(message, LogLevel.ERROR, oneTime)
    }
}