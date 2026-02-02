package liltojustice.trueadaptivemusic

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory
import java.nio.file.Files
import kotlin.io.path.*

class TrueAdaptiveMusic: ModInitializer {
    @OptIn(ExperimentalPathApi::class)
    override fun onInitialize() {
        Files.createDirectories(Constants.MUSIC_PACK_DIR)
        Files.createDirectories(Constants.OPTIONS_DIR)

        if (!Constants.OPTIONS_PATH.exists()) {
            Files.createFile(Constants.OPTIONS_PATH)
        }
    }

    companion object {
        val LOGGER: org.slf4j.Logger = LoggerFactory.getLogger(TrueAdaptiveMusic::class.java)
    }
}
