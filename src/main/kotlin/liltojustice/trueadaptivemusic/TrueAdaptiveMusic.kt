package liltojustice.trueadaptivemusic

import net.fabricmc.api.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.*

class TrueAdaptiveMusic: ModInitializer {
    @OptIn(ExperimentalPathApi::class)
    override fun onInitialize() {
        Files.createDirectories(Constants.MUSIC_PACK_DIR)
        Files.createDirectories(Constants.FFMPEG_DIR)
        Files.createDirectories(Constants.PACK_BROWSER_CACHE_DIR)

        if (isWindows) {
            cloneResourceFile(Constants.FFMPEG_WINDOWS_PATH, Constants.FFMPEG_WINDOWS_RESOURCE)
            cloneResourceFile(Constants.FFPROBE_WINDOWS_PATH, Constants.FFPROBE_WINDOWS_RESOURCE)
        }
        else {
            cloneResourceFile(Constants.FFMPEG_PATH, Constants.FFMPEG_RESOURCE)
            cloneResourceFile(Constants.FFPROBE_PATH, Constants.FFPROBE_RESOURCE)
        }
    }

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(TrueAdaptiveMusic::class.java)
        val POSIX_PERMISSIONS: FileAttribute<Set<PosixFilePermission>> = PosixFilePermissions.asFileAttribute(
            PosixFilePermissions.fromString("rwxrwxrwx"))
        val isWindows = "windows" in System.getProperty("os.name").lowercase()

        fun cloneResourceFile(destinationPath: Path, resource: String) {
            destinationPath.takeIf { !it.exists() }?.let { filePath ->
                if (isWindows) {
                    Files.createFile(filePath)
                }
                else {
                    Files.createFile(filePath, POSIX_PERMISSIONS)
                }

                this::class.java.classLoader.getResourceAsStream(resource).use {
                    it?.copyTo(filePath.outputStream())
                }
            }
        }
    }
}
