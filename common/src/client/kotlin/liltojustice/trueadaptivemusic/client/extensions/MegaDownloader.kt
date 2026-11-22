package liltojustice.trueadaptivemusic.client.extensions

import liltojustice.trueadaptivemusic.Reference
import java.nio.file.Path

typealias MegaDownloader = suspend (url: String, outputPath: Path, progressOutput: Reference<Float>?) -> Unit