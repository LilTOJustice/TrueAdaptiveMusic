package liltojustice.trueadaptivemusic

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier
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
        val NULL_IDENTIFIER = Identifier.fromNamespaceAndPath("trueadaptivemusic", "null")
        val REQUIRES_SERVER_SUPPORT_TRIGGERS = listOf(
            "structure", "structure_set", "spawn_point_nearby", "scoreboard", "custom")
        val REQUIRES_SERVER_SUPPORT_TEXT = "*${Component.translatableWithFallback(
            "trueadaptivemusic.requires_server",
            "Host must have TAM installed!"
        ).string}*"
        val REQUIRES_SERVER_SUPPORT_TOOLTIP = Component.translatableWithFallback(
            "trueadaptivemusic.requires_server_tooltip",
            "This trigger type will only work properly in singleplayer, or if the host of the world " +
                    "(or server) has True Adaptive Music installed."
        )
        val ALL_ALLOWED_FILE_TYPES = setOf("wav", "flac", "ogg", "mp3")
        const val TAM_ICON_RESOURCE_PATH = "assets/trueadaptivemusic/icon.png"
        const val RULES_FILENAME = "rules.json"
        const val PACK_OPTIONS_FILENAME = "options.json"
        const val META_FILENAME = "meta.json"
        const val ICON_FILENAME = "icon.png"
        const val ASSETS_DIRNAME = "assets"
        const val PREDICATES_DIRNAME = "predicates"
        const val WIKI_LINK = "https://liltojustice.github.io/TrueAdaptiveMusic/"
        const val DISCORD_JOIN_URL = "https://discord.gg/v64K4hNdXu"
        const val TAM_MODRINTH_URL = "https://modrinth.com/project/true-adaptive-music"
    }
}