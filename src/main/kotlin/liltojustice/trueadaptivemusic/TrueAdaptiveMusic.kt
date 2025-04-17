package liltojustice.trueadaptivemusic

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory
import java.nio.file.Files
import kotlin.io.path.*

class TrueAdaptiveMusic: ModInitializer {
    @OptIn(ExperimentalPathApi::class)
    override fun onInitialize() {
        Files.createDirectories(Path(Constants.MUSIC_PACK_DIR))

        val optionsFilePath = Path(Constants.OPTIONS_FILENAME)

        if (!optionsFilePath.exists()) {
            Files.createFile(optionsFilePath)
        }
    }

    companion object {
        val LOGGER: org.slf4j.Logger = LoggerFactory.getLogger(TrueAdaptiveMusic::class.java)
    }
}
