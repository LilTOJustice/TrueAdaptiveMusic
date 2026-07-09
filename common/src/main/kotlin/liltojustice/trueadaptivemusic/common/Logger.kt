package liltojustice.trueadaptivemusic.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Logger {
    private val LOGGER: Logger = LoggerFactory.getLogger(TAMMainInitializer::class.java)
    private val oneTimeLogs = mutableSetOf<String>()
    private fun log(message: String, logLevel: LogLevel = LogLevel.INFO, oneTime: Boolean = false) {
        if (oneTimeLogs.contains(message)) {
            return
        }

        if (oneTime) {
            oneTimeLogs.add(message)
        }

        when(logLevel) {
            LogLevel.INFO -> LOGGER.info(message)
            LogLevel.WARNING -> LOGGER.warn(message)
            LogLevel.ERROR -> LOGGER.error(message)
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