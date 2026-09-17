package liltojustice.trueadaptivemusicextensions.client.browser

import kotlinx.coroutines.coroutineScope
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.Reference
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusicextensions.client.ExtensionConstants
import net.minecraft.util.Util.OS
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
                            getGigaGrabber(), url, "--output_path", outputPath.invariantSeparatorsPathString)
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
                catch (_: Exception) { }
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

    private fun getGigaGrabber(): String {
        return when(TAMClient.platform) {
            OS.WINDOWS -> ExtensionConstants.GIGA_GRABBER_WINDOWS_PATH.invariantSeparatorsPathString
            else -> ExtensionConstants.GIGA_GRABBER_PATH.invariantSeparatorsPathString
        }
    }
}