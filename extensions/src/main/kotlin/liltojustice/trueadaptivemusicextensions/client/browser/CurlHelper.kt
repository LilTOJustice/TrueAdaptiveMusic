package liltojustice.trueadaptivemusicextensions.client.browser

import kotlinx.coroutines.coroutineScope
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.Reference
import java.nio.file.Path
import kotlin.io.path.invariantSeparatorsPathString

object CurlHelper {
    suspend fun get(url: String, outputPath: Path, progressOutput: Reference<Float>? = null) {
        coroutineScope {
            processRequest(
                Runtime
                    .getRuntime()
                    .exec(
                        arrayOf(
                            "curl", "-L", "-o", outputPath.invariantSeparatorsPathString, url, "--progress-bar")
                    ),
                progressOutput
            )
        }
    }

    private fun processRequest(process: Process, progressOutput: Reference<Float>?) {
        val progressReaderThread = Thread {
            process.errorReader().use { reader ->
                try {
                    while (true) {
                        reader.readLine()?.filter { char -> char.isDigit() || char == '.' }?.toFloatOrNull()?.let {
                            progressOutput?.value = it / 100F
                        }
                    }
                }
                catch (_: Exception) { }
            }
        }

        progressReaderThread.start()

        try {
            process.waitFor()
        }
        catch (e: Exception) {
            process.destroyForcibly()

            Logger.logError("Curl error:\n${e.stackTraceToString()}")
        }

        process.errorStream.close()
        progressReaderThread.join()
    }
}