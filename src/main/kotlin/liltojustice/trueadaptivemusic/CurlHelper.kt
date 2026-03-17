package liltojustice.trueadaptivemusic

import kotlinx.coroutines.coroutineScope
import java.nio.file.Path
import kotlin.io.path.invariantSeparatorsPathString

object CurlHelper {
    suspend fun curl(url: String, outputPath: Path) {
        coroutineScope {
            Runtime
                .getRuntime()
                .exec(arrayOf("curl", "-L", "-o", outputPath.invariantSeparatorsPathString, "\"${url}\""))
                .waitFor()
        }
    }
}