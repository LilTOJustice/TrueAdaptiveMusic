package liltojustice.trueadaptivemusic.common.client

import liltojustice.trueadaptivemusic.common.Reference
import liltojustice.trueadaptivemusic.common.client.browser.BrowsableMusicPack
import liltojustice.trueadaptivemusic.common.client.browser.PackManifest
import java.nio.file.Path

data class Extensions(
    val curlDownloader: suspend (url: String, outputPath: Path, progressOutput: Reference<Float>?) -> Unit,
    val megaDownloader: suspend (url: String, outputPath: Path, progressOutput: Reference<Float>?) -> Unit,
    val packFetcher: suspend (ignoreCache: Boolean) -> PackManifest?,
    val packDownloader: suspend (musicPack: BrowsableMusicPack, progressOutput: Reference<Float>) -> Unit
)