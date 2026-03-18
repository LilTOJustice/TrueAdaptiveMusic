package liltojustice.trueadaptivemusic.client.music.pack.browsable

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.CurlHelper
import liltojustice.trueadaptivemusic.Reference
import java.nio.file.Path
import java.util.zip.ZipException
import java.util.zip.ZipFile
import kotlin.io.path.deleteIfExists

object BrowsableMusicPackDownloader {
    suspend fun downloadMusicPack(musicPack: BrowsableMusicPack, progressOutput: Reference<Float>) {
        if (musicPack.source.startsWith(Constants.DISCORD_SOURCE_PREFIX))
            downloadFromDiscord(musicPack, progressOutput)
        else if (musicPack.source.startsWith(Constants.DRIVE_SOURCE_PREFIX))
            downloadFromGoogleDrive(musicPack, progressOutput)
        else
            throw Exception(
                "Failed to get curl target for pack ${musicPack.name} with source ${musicPack.source}.")
    }

    private suspend fun downloadFromDiscord(musicPack: BrowsableMusicPack, progressOutput: Reference<Float>) {
        downloadPack(musicPack.source, musicPack.getFilePath(), progressOutput)
    }

    private suspend fun downloadFromGoogleDrive(musicPack: BrowsableMusicPack, progressOutput: Reference<Float>) {
        val targetUrl =
            Constants.DRIVE_SOURCE_DOWNLOAD_PREFIX +
                    musicPack.source.split("/").takeLast(2).first() +
                    Constants.DRIVE_SOURCE_DOWNLOAD_SUFFIX
        downloadPack(targetUrl, musicPack.getFilePath(), progressOutput)
    }

    private suspend fun downloadPack(url: String, outputPath: Path, progressOutput: Reference<Float>) {
        CurlHelper.curl(url, outputPath, progressOutput)
        try {
            ZipFile(outputPath.toFile())
        }
        catch (e: ZipException) {
            outputPath.deleteIfExists()
            throw MusicPackDownloadException("Downloaded file was not a valid zip file.", e)
        }
    }
}