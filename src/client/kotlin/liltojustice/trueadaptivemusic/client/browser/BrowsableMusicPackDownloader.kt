package liltojustice.trueadaptivemusic.client.browser

import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.Reference
import liltojustice.trueadaptivemusic.client.serialization.EnumTypeAdapter
import java.nio.file.Path
import java.util.Calendar
import java.util.zip.ZipException
import java.util.zip.ZipFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.moveTo

object BrowsableMusicPackDownloader {
    private const val DRIVE_SOURCE_DOWNLOAD_PREFIX = "https://drive.usercontent.google.com/download?id="
    private const val DRIVE_SOURCE_DOWNLOAD_SUFFIX = "&export=download&confirm=y"
    private const val MEGA_SOURCE_DOWNLOAD_PREFIX = "https://mega.nz/file/"
    private const val MANIFEST_FILE_URL =
        "https://gist.githubusercontent.com/LilTOJustice/d591ee8817ee4051acdc76ed5ff092b1/raw/manifest.json"

    suspend fun fetchPacksFromRepository(ignoreCache: Boolean = false): PackManifest? {
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .registerTypeAdapter(
                BrowsableMusicPack.SourceType::class.java,
                EnumTypeAdapter(BrowsableMusicPack.SourceType::class)
            ).create()
        if (!ignoreCache && Constants.MANIFEST_PATH.exists()) {
            return gson
                .fromJson(Constants.MANIFEST_PATH.toFile().readText(), PackManifest::class.java)
        }

        coroutineScope { CurlHelper.get(MANIFEST_FILE_URL, Constants.MANIFEST_PATH_TEMP) }

        if (!Constants.MANIFEST_PATH_TEMP.exists()) {
            Logger.logError("Failed to fetch pack manifest.")

            return null
        }

        val manifest = try {
            gson
                .fromJson(Constants.MANIFEST_PATH_TEMP.toFile().readText(), PackManifest::class.java)
                .copy(timestamp = Calendar.getInstance().time)
        }
        catch (_: JsonSyntaxException) {
            Logger.logError("Failed to parse manifest json.")

            return null
        }

        Constants.MANIFEST_PATH_TEMP.moveTo(Constants.MANIFEST_PATH, true)
        Constants.MANIFEST_PATH.toFile().writeText(gson.toJson(manifest))

        manifest.packs.forEach { pack ->
            pack.getImagePath()?.let { imagePath ->
                pack.image?.source?.let { source ->
                    CurlHelper.get(source, imagePath)
                }
            }
        }

        return manifest
    }

    suspend fun downloadMusicPack(musicPack: BrowsableMusicPack, progressOutput: Reference<Float>) {
        when (musicPack.sourceType) {
            BrowsableMusicPack.SourceType.Discord -> downloadFromDiscord(musicPack, progressOutput)
            BrowsableMusicPack.SourceType.GDrive -> downloadFromGoogleDrive(musicPack, progressOutput)
            BrowsableMusicPack.SourceType.MEGA -> downloadFromMega(musicPack, progressOutput)
        }
    }

    private suspend fun downloadFromDiscord(musicPack: BrowsableMusicPack, progressOutput: Reference<Float>) {
        downloadPackCurl(musicPack.source, musicPack.getFilePath(), progressOutput)
    }

    private suspend fun downloadFromGoogleDrive(musicPack: BrowsableMusicPack, progressOutput: Reference<Float>) {
        val targetUrl = DRIVE_SOURCE_DOWNLOAD_PREFIX + musicPack.source + DRIVE_SOURCE_DOWNLOAD_SUFFIX
        downloadPackCurl(targetUrl, musicPack.getFilePath(), progressOutput)
    }

    private suspend fun downloadFromMega(musicPack: BrowsableMusicPack, progressOutput: Reference<Float>) {
        val targetUrl = MEGA_SOURCE_DOWNLOAD_PREFIX + musicPack.source
        downloadPackMega(targetUrl, musicPack.getFilePath(), progressOutput)
    }

    private suspend fun downloadPackCurl(url: String, outputPath: Path, progressOutput: Reference<Float>) {
        CurlHelper.get(url, outputPath, progressOutput)
        verifyExists(outputPath)
    }

    private suspend fun downloadPackMega(url: String, outputPath: Path, progressOutput: Reference<Float>) {
        MegaHelper.get(url, outputPath, progressOutput)
        verifyExists(outputPath)
    }

    private suspend fun verifyExists(outputPath: Path) {
        try {
            withContext(Dispatchers.IO) { ZipFile(outputPath.toFile()).use { it.close() } }
        }
        catch (e: ZipException) {
            outputPath.deleteIfExists()
            throw MusicPackDownloadException("Downloaded file was not a valid zip file.", e)
        }
        catch (e: Exception) {
            outputPath.deleteIfExists()
            throw MusicPackDownloadException("Failed to download pack.", e)
        }
    }
}