package liltojustice.trueadaptivemusic

import java.nio.file.Path
import kotlin.io.path.invariantSeparatorsPathString

object CurlHelper {
    fun curl(url: String, outputPath: Path) {
        Runtime
            .getRuntime()
            .exec(arrayOf("curl", "-L", "-o", outputPath.invariantSeparatorsPathString, "\"${url}\""))
            .waitFor()
    }
}