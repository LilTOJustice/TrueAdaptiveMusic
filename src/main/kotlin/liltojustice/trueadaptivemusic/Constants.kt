package liltojustice.trueadaptivemusic

import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
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
        val DISCORD_JOIN_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.join_discord", "Join the Discord!")
        val POSIX_PERMISSIONS: FileAttribute<Set<PosixFilePermission>> = PosixFilePermissions.asFileAttribute(
            PosixFilePermissions.fromString("rwxrwxrwx"))
        val NULL_IDENTIFIER: Identifier = Identifier.of("trueadaptivemusic", "null")
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
        const val PREDICATES_DIRNAME = "predicates"
        const val WIKI_LINK = "https://liltojustice.github.io/TrueAdaptiveMusic/"
        const val DISCORD_JOIN_URL = "https://discord.gg/v64K4hNdXu"
    }
}