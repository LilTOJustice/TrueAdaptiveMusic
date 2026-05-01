package liltojustice.trueadaptivemusic.client.browser

import kotlin.io.path.Path
import kotlin.io.path.invariantSeparatorsPathString

class Constants {
    companion object {
        val MUSIC_PACK_DIR = Path("trueadaptivemusicpacks")
        val PACK_BROWSER_CACHE_DIR = Path(".trueadaptivemusiccache")
        val MANIFEST_PATH = Path(
            PACK_BROWSER_CACHE_DIR.invariantSeparatorsPathString, "manifest.json")
        val MANIFEST_PATH_TEMP = Path(
            PACK_BROWSER_CACHE_DIR.invariantSeparatorsPathString, "manifest.json.tmp")
        const val DRIVE_SOURCE_DOWNLOAD_PREFIX = "https://drive.usercontent.google.com/download?id="
        const val DRIVE_SOURCE_DOWNLOAD_SUFFIX = "&export=download&confirm=y"
        const val MANIFEST_FILE_URL =
            "https://gist.githubusercontent.com/LilTOJustice/d591ee8817ee4051acdc76ed5ff092b1/raw/manifest.json"
    }
}