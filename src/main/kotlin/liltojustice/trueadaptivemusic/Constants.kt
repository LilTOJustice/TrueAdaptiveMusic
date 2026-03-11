package liltojustice.trueadaptivemusic

import kotlin.io.path.Path
import kotlin.io.path.pathString

class Constants {
    companion object {
        val MUSIC_PACK_DIR = Path("trueadaptivemusicpacks")
        val OPTIONS_DIR = Path("config", "trueadaptivemusic")
        val OPTIONS_PATH = Path(OPTIONS_DIR.pathString, "trueadaptivemusic.json")
        val FFMPEG_PATH = Path(OPTIONS_DIR.pathString, "ffmpeg.exe")
        val FFPROBE_PATH = Path(OPTIONS_DIR.pathString, "ffprobe.exe")
        const val RULES_FILENAME = "rules.json"
        const val PACK_OPTIONS_FILENAME = "options.json"
        const val META_FILENAME = "meta.json"
        const val ASSETS_DIRNAME = "assets"
        const val WIKI_LINK = "https://liltojustice.github.io/TrueAdaptiveMusic/"
        const val FFMPEG_DOWNLOAD_LINK = "https://www.gyan.dev/ffmpeg/builds/"
    }

    class Colors {
        companion object {
            const val GREEN = 0x00FF00
            const val YELLOW = 0xFFFF00
        }
    }
}