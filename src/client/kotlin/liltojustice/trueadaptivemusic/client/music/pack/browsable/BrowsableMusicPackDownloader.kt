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
        when (musicPack.sourceType) {
            BrowsableMusicPack.SourceType.Discord -> downloadFromDiscord(musicPack, progressOutput)
            BrowsableMusicPack.SourceType.GDrive -> downloadFromGoogleDrive(musicPack, progressOutput)
        }
    }

    private suspend fun downloadFromDiscord(musicPack: BrowsableMusicPack, progressOutput: Reference<Float>) {
        downloadPack(musicPack.source, musicPack.getFilePath(), progressOutput)
    }

    private suspend fun downloadFromGoogleDrive(musicPack: BrowsableMusicPack, progressOutput: Reference<Float>) {
        val targetUrl =
            Constants.DRIVE_SOURCE_DOWNLOAD_PREFIX + musicPack.source + Constants.DRIVE_SOURCE_DOWNLOAD_SUFFIX
        downloadPack(targetUrl, musicPack.getFilePath(), progressOutput)
    }

    private suspend fun downloadPack(url: String, outputPath: Path, progressOutput: Reference<Float>) {
        CurlHelper.curl(url, outputPath, progressOutput)
        try {
            ZipFile(outputPath.toFile()).close()
        }
        catch (e: ZipException) {
            outputPath.deleteIfExists()
            throw MusicPackDownloadException("Downloaded file was not a valid zip file.", e)
        }
    }
}