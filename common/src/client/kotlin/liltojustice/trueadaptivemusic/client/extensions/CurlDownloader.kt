package liltojustice.trueadaptivemusic.client.extensions

import liltojustice.trueadaptivemusic.Reference
import java.nio.file.Path

typealias CurlDownloader = suspend (url: String, outputPath: Path, progressOutput: Reference<Float>?) -> Unit