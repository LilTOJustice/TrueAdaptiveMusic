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
        Files.createDirectories(Constants.PACK_BROWSER_CACHE_DIR)

        Constants.FFMPEG_WINDOWS_PATH.toFile().takeIf { !it.exists() }?.let { file ->
            file.createNewFile()
            this::class.java.classLoader.getResourceAsStream(Constants.FFMPEG_WINDOWS_RESOURCE_PATH).use {
                it?.copyTo(file.outputStream())
            }
        }

        Constants.FFPROBE_WINDOWS_PATH.toFile().takeIf { !it.exists() }?.let { file ->
            file.createNewFile()
            this::class.java.classLoader.getResourceAsStream(Constants.FFPROBE_WINDOWS_RESOURCE_PATH).use {
                it?.copyTo(file.outputStream())
            }
        }

        Constants.FFMPEG_PATH.toFile().takeIf { !it.exists() }?.let { file ->
            file.createNewFile()
            this::class.java.classLoader.getResourceAsStream(Constants.FFMPEG_RESOURCE_PATH).use {
                it?.copyTo(file.outputStream())
            }
        }

        Constants.FFPROBE_PATH.toFile().takeIf { !it.exists() }?.let { file ->
            file.createNewFile()
            this::class.java.classLoader.getResourceAsStream(Constants.FFPROBE_RESOURCE_PATH).use {
                it?.copyTo(file.outputStream())
            }
        }
    }

    companion object {
        val LOGGER: org.slf4j.Logger = LoggerFactory.getLogger(TrueAdaptiveMusic::class.java)
    }
}
