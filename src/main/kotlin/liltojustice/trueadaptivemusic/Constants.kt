package liltojustice.trueadaptivemusic

import net.minecraft.util.Identifier

class Constants {
    companion object {
        const val MUSIC_PACK_DIR = "trueadaptivemusicpacks"
        const val OPTIONS_FILENAME = ".trueadaptivemusic.json"
        const val RULES_FILENAME = "rules.json"
        const val META_FILENAME = "meta.json"
        const val ASSETS_DIRNAME = "assets"
        const val WIKI_LINK = "https://liltojustice.github.io/TrueAdaptiveMusic/"
        val AUDIO_FILE_STREAM_ID = Identifier("trueadaptivemusic:audiofilestream")
    }

    class Colors {
        companion object {
            const val GREEN = 0x00FF00
            const val YELLOW = 0xFFFF00
        }
    }
}