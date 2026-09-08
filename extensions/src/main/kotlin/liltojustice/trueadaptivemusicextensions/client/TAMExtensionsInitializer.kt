package liltojustice.trueadaptivemusicextensions.client

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.Extensions
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusicextensions.client.browser.BrowsableMusicPackDownloader
import liltojustice.trueadaptivemusicextensions.client.browser.CurlHelper
import liltojustice.trueadaptivemusicextensions.client.browser.MegaHelper
import net.minecraft.util.Util.OS
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream

@Suppress("UNUSED")
object TAMExtensionsInitializer {
    fun initialize(): Extensions {
        Files.createDirectories(Constants.FFMPEG_DIR)
        Files.createDirectories(ExtensionConstants.GIGA_GRABBER_DIR)

        when(TAMClient.platform) {
            OS.WINDOWS -> initWindows()
            OS.LINUX -> initLinux()
            OS.OSX -> initMac()
            else -> initOther()
        }

        return Extensions(
            CurlHelper::get,
            MegaHelper::get,
            BrowsableMusicPackDownloader::fetchPacksFromRepository,
            BrowsableMusicPackDownloader::downloadMusicPack
        )
    }

    private fun initWindows() {
        cloneResourceFile(Constants.FFMPEG_WINDOWS_PATH, ExtensionConstants.FFMPEG_WINDOWS_RESOURCE)
        cloneResourceFile(Constants.FFPROBE_WINDOWS_PATH, ExtensionConstants.FFPROBE_WINDOWS_RESOURCE)
        cloneResourceFile(
            ExtensionConstants.LIBWINPTHREAD_WINDOWS_PATH,
            ExtensionConstants.LIBWINPTHREAD_WINDOWS_RESOURCE
        )
        cloneResourceFile(
            ExtensionConstants.GIGA_GRABBER_WINDOWS_PATH,
            ExtensionConstants.GIGA_GRABBER_WINDOWS_RESOURCE
        )
    }

    private fun initLinux() {
        cloneResourceFile(Constants.FFMPEG_PATH, ExtensionConstants.FFMPEG_LINUX_RESOURCE)
        cloneResourceFile(Constants.FFPROBE_PATH, ExtensionConstants.FFPROBE_LINUX_RESOURCE)
        cloneResourceFile(
            ExtensionConstants.GIGA_GRABBER_PATH, ExtensionConstants.GIGA_GRABBER_LINUX_RESOURCE)
    }

    private fun initMac() {
        cloneResourceFile(Constants.FFMPEG_PATH, ExtensionConstants.FFMPEG_MAC_RESOURCE)
        cloneResourceFile(Constants.FFPROBE_PATH, ExtensionConstants.FFPROBE_MAC_RESOURCE)
        cloneResourceFile(
            ExtensionConstants.GIGA_GRABBER_PATH, ExtensionConstants.GIGA_GRABBER_MAC_RESOURCE)
    }

    private fun initOther() {
    }

    private fun cloneResourceFile(destinationPath: Path, resource: String) {
        destinationPath.deleteIfExists()

        if (TAMClient.platform == OS.WINDOWS) {
            Files.createFile(destinationPath)
        } else {
            Files.createFile(destinationPath, ExtensionConstants.POSIX_PERMISSIONS)
        }

        this::class.java.classLoader.getResourceAsStream(resource)?.use { resourceStream ->
            destinationPath.outputStream().use { destinationStream ->
                resourceStream.copyTo(destinationStream)
            }
        }
    }
}
