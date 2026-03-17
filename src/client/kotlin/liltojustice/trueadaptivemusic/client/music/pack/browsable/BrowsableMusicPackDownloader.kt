package liltojustice.trueadaptivemusic.client.music.pack.browsable

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.CurlHelper

object BrowsableMusicPackDownloader {
    suspend fun downloadMusicPack(musicPack: BrowsableMusicPack) {
        if (musicPack.source.startsWith(Constants.DISCORD_SOURCE_PREFIX))
            downloadFromDiscord(musicPack)
        else if (musicPack.source.startsWith(Constants.DRIVE_SOURCE_PREFIX))
            downloadFromGoogleDrive(musicPack)
        else
            throw Exception(
                "Failed to get curl target for pack ${musicPack.name} with source ${musicPack.source}.")
    }

    private suspend fun downloadFromDiscord(musicPack: BrowsableMusicPack) {
        CurlHelper.curl(musicPack.source, musicPack.getFilePath())
    }

    private suspend fun downloadFromGoogleDrive(musicPack: BrowsableMusicPack) {
        val targetUrl =
            Constants.DRIVE_SOURCE_DOWNLOAD_PREFIX +
                    musicPack.source.split("/").takeLast(2).first() +
                    Constants.DRIVE_SOURCE_DOWNLOAD_SUFFIX
        CurlHelper.curl(targetUrl, musicPack.getFilePath())
    }
}