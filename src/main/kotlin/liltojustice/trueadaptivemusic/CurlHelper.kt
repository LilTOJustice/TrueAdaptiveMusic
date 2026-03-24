package liltojustice.trueadaptivemusic

import kotlinx.coroutines.coroutineScope
import kotlinx.io.IOException
import java.nio.file.Path
import kotlin.io.path.invariantSeparatorsPathString

object CurlHelper {
    suspend fun curl(url: String, outputPath: Path, progressOutput: Reference<Float>? = null) {
        coroutineScope {
            val process = Runtime
                .getRuntime()
                .exec(
                    arrayOf(
                        "curl", "-L", "-o", outputPath.invariantSeparatorsPathString, "\"${url}\"", "--progress-bar")
                )

            val progressReaderThread = Thread {
                process.errorReader().use { reader ->
                    try {
                        while (true) {
                            reader.readLine()?.filter { char -> char.isDigit() || char == '.' }?.toFloatOrNull()?.let {
                                progressOutput?.value = it / 100F
                            }
                        }
                    }
                    catch (_: IOException) { }
                }
            }

            progressReaderThread.start()

            process.waitFor()
            process.errorStream.close()
            progressReaderThread.join()
        }
    }
}