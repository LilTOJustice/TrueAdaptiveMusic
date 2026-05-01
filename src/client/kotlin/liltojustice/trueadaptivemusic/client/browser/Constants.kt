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
    }
}