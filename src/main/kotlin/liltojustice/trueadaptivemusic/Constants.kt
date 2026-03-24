package liltojustice.trueadaptivemusic

import net.minecraft.text.MutableText
import net.minecraft.text.Text
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
        val MANIFEST_PATH = Path(PACK_BROWSER_CACHE_DIR.invariantSeparatorsPathString, "manifest.json")
        val MANIFEST_PATH_TEMP = Path(
            PACK_BROWSER_CACHE_DIR.invariantSeparatorsPathString, "manifest.json.tmp")
        val DISCORD_JOIN_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.join_discord", "Join the Discord!")
        const val RULES_FILENAME = "rules.json"
        const val PACK_OPTIONS_FILENAME = "options.json"
        const val META_FILENAME = "meta.json"
        const val LOGO_FILENAME = "logo.png"
        const val ASSETS_DIRNAME = "assets"
        const val WIKI_LINK = "https://liltojustice.github.io/TrueAdaptiveMusic/"
        const val FFMPEG_DOWNLOAD_LINK = "https://www.gyan.dev/ffmpeg/builds/"
        const val DRIVE_SOURCE_DOWNLOAD_PREFIX = "https://drive.usercontent.google.com/download?id="
        const val DRIVE_SOURCE_DOWNLOAD_SUFFIX = "&export=download&confirm=y"
        const val MANIFEST_FILE_URL =
            "https://gist.githubusercontent.com/LilTOJustice/d591ee8817ee4051acdc76ed5ff092b1/raw/manifest.json"
        const val DISCORD_JOIN_URL = "https://discord.gg/v64K4hNdXu"
    }
}