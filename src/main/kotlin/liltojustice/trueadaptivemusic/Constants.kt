package liltojustice.trueadaptivemusic

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import kotlin.io.path.Path
import kotlin.io.path.invariantSeparatorsPathString

class Constants {
    companion object {
        const val ROOT_PREDICATE_NAME = "root"
        const val ERROR_PREDICATE_NAME = "error_predicate"
        const val ERROR_EVENT_NAME = "error_event"
        val MUSIC_PACK_DIR = Path("trueadaptivemusicpacks")
        val OPTIONS_DIR = Path("config", "trueadaptivemusic")
        val FFMPEG_DIR = Path(OPTIONS_DIR.invariantSeparatorsPathString, "ffmpeg_binaries")
        val OPTIONS_PATH = Path(OPTIONS_DIR.invariantSeparatorsPathString, "trueadaptivemusic.json")
        val FFMPEG_WINDOWS_PATH = Path(FFMPEG_DIR.invariantSeparatorsPathString, "ffmpeg.exe")
        val FFPROBE_WINDOWS_PATH = Path(FFMPEG_DIR.invariantSeparatorsPathString, "ffprobe.exe")
        val FFMPEG_PATH = Path(FFMPEG_DIR.invariantSeparatorsPathString, "ffmpeg")
        val FFPROBE_PATH = Path(FFMPEG_DIR.invariantSeparatorsPathString, "ffprobe")
        val PACK_BROWSER_CACHE_DIR = Path(".trueadaptivemusiccache")
        val DISCORD_JOIN_TEXT: MutableComponent = Component.translatableWithFallback(
            "trueadaptivemusic.join_discord", "Join the Discord!")
        const val TAM_ICON_RESOURCE_PATH = "assets/trueadaptivemusic/icon.png"
        const val FFMPEG_WINDOWS_RESOURCE = "assets/trueadaptivemusic/ffmpeg/ffmpeg.exe"
        const val FFPROBE_WINDOWS_RESOURCE = "assets/trueadaptivemusic/ffmpeg/ffprobe.exe"
        const val FFMPEG_RESOURCE = "assets/trueadaptivemusic/ffmpeg/ffmpeg"
        const val FFPROBE_RESOURCE = "assets/trueadaptivemusic/ffmpeg/ffprobe"
        const val RULES_FILENAME = "rules.json"
        const val PACK_OPTIONS_FILENAME = "options.json"
        const val META_FILENAME = "meta.json"
        const val ICON_FILENAME = "icon.png"
        const val ASSETS_DIRNAME = "assets"
        const val WIKI_LINK = "https://liltojustice.github.io/TrueAdaptiveMusic/"
        const val DISCORD_JOIN_URL = "https://discord.gg/v64K4hNdXu"
        const val MODRINTH_PACK_BROWSER_URL = "https://modrinth.com/project/trueadaptivemusicpackbrowser"
    }
}