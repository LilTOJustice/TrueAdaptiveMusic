package liltojustice.trueadaptivemusic

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.exists

class TrueAdaptiveMusic: ModInitializer {
    override fun onInitialize() {
        Files.createDirectories(Path(Constants.MUSIC_PACK_DIR))
        Files.createDirectories(Path(Constants.MISC_DIR))

        val selectedPackFile = Path(Constants.SELECTED_PACK)

        if (!selectedPackFile.exists())
        {
            Files.createFile(selectedPackFile)
        }
    }

    companion object {
        val LOGGER: org.slf4j.Logger = LoggerFactory.getLogger(TrueAdaptiveMusic::class.java)
    }
}
