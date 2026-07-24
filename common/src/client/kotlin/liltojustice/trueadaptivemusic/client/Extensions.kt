package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.Reference
import liltojustice.trueadaptivemusic.client.browser.BrowsableMusicPack
import liltojustice.trueadaptivemusic.client.browser.PackManifest
import java.nio.file.Path

data class Extensions(
    val curlDownloader: suspend (url: String, outputPath: Path, progressOutput: Reference<Float>?) -> Unit,
    val megaDownloader: suspend (url: String, outputPath: Path, progressOutput: Reference<Float>?) -> Unit,
    val packFetcher: suspend (ignoreCache: Boolean) -> PackManifest?,
    val packDownloader: suspend (musicPack: BrowsableMusicPack, progressOutput: Reference<Float>) -> Unit
)