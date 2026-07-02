package liltojustice.trueadaptivemusic.client.browser

import kotlinx.coroutines.coroutineScope
import kotlinx.io.IOException
import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.Reference
import java.nio.file.Path
import kotlin.io.path.invariantSeparatorsPathString

object MegaHelper {
    suspend fun get(url: String, outputPath: Path, progressOutput: Reference<Float>? = null) {
        coroutineScope {
            processRequest(
                Runtime
                    .getRuntime()
                    .exec(
                        arrayOf(
                            Constants.GIGA_GRABBER_WINDOWS_PATH.invariantSeparatorsPathString, url,
                            "--output_path", outputPath.invariantSeparatorsPathString
                        )
                    ),
                progressOutput
            )
        }
    }

    private fun processRequest(process: Process, progressOutput: Reference<Float>?) {
        val progressReaderThread = Thread {
            process.inputReader().use { reader ->
                try {
                    while (true) {
                        reader.readLine()?.toFloatOrNull()?.let { progressOutput?.value = it }
                    }
                }
                catch (_: IOException) { }
            }
        }

        progressReaderThread.start()

        try {
            process.waitFor()
        }
        catch (e: Exception) {
            process.destroyForcibly()

            Logger.logError("Mega error:\n${e.stackTraceToString()}")
        }

        process.inputStream.close()
        progressReaderThread.join()
    }
}