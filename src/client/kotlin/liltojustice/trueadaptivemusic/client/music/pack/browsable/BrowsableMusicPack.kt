package liltojustice.trueadaptivemusic.client.music.pack.browsable

import liltojustice.trueadaptivemusic.Constants
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.invariantSeparatorsPathString

data class BrowsableMusicPack(
    val name: String,
    val version: String?,
    val description: String?,
    val source: String,
    val image: Image?,
    val size: Int
) {
    fun getFilePath(): Path {
        return Path(
            Constants.MUSIC_PACK_DIR.invariantSeparatorsPathString,
            name.replace(fileNameRegex, "") + (version?.let { "-${it}" } ?: "") + ".zip"
        )
    }

    fun getImagePath(): Path? {
        return image?.let {
            Path(
                Constants.PACK_BROWSER_CACHE_DIR.invariantSeparatorsPathString,
                name.replace(fileNameRegex, "") + '.' + image.extension
            )
        }
    }

    companion object {
        private val fileNameRegex = Regex("[^ a-zA-Z0-9.\\-_/\\\\]")
    }
}