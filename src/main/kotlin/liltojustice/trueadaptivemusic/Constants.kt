package liltojustice.trueadaptivemusic

import kotlin.io.path.Path
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.pathString

class Constants {
    companion object {
        val MUSIC_PACK_DIR = Path("trueadaptivemusicpacks")
        val OPTIONS_DIR = Path("config", "trueadaptivemusic")
        val OPTIONS_PATH = Path(OPTIONS_DIR.pathString, "trueadaptivemusic.json")
        val FFMPEG_PATH = Path(OPTIONS_DIR.pathString, "ffmpeg.exe")
        val FFPROBE_PATH = Path(OPTIONS_DIR.pathString, "ffprobe.exe")
        val PACK_BROWSER_CACHE_DIR = Path(".trueadaptivemusiccache")
        val MANIFEST_PATH = Path(PACK_BROWSER_CACHE_DIR.invariantSeparatorsPathString, "manifest.txt")
        const val RULES_FILENAME = "rules.json"
        const val PACK_OPTIONS_FILENAME = "options.json"
        const val META_FILENAME = "meta.json"
        const val ASSETS_DIRNAME = "assets"
        const val WIKI_LINK = "https://liltojustice.github.io/TrueAdaptiveMusic/"
        const val FFMPEG_DOWNLOAD_LINK = "https://www.gyan.dev/ffmpeg/builds/"
        const val GOOGLE_DRIVE_LINK = "https://drive.google.com"
        const val DISCORD_SOURCE_PREFIX = "https://cdn.discordapp.com"
        const val DRIVE_SOURCE_PREFIX = GOOGLE_DRIVE_LINK
        const val DRIVE_SOURCE_DOWNLOAD_PREFIX = "https://docs.google.com/uc?export=download&id="
        const val MANIFEST_FILE_ID = "1vj8zK9-OwAxmHuxJXqyJPXTE--1VZY3I"
    }
}