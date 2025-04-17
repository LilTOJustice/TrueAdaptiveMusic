package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.LogLevel
import liltojustice.trueadaptivemusic.Logger.Companion.log
import liltojustice.trueadaptivemusic.TrueAdaptiveMusicOptions
import liltojustice.trueadaptivemusic.client.music.MusicLoadException
import liltojustice.trueadaptivemusic.client.music.MusicManager
import liltojustice.trueadaptivemusic.client.music.MusicPack
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import net.minecraft.client.MinecraftClient
import net.minecraft.util.JsonHelper
import java.io.IOException
import kotlin.io.path.Path

object TAMClient {
    private var initialized = false
    private var musicManager: MusicManager? = null
    private var options: TrueAdaptiveMusicOptions = TrueAdaptiveMusicOptions()

    var musicPack: MusicPack?
        get() = musicManager?.getMusicPack()
        set(value) {
            musicManager?.selectMusicPack(value)

            val packName = value?.packName ?: ""
            try {
                options.selectedPack = packName
                options.save()
            } catch (ignored: IOException) {
                log("Failed to save selected pack \"$packName\"", LogLevel.ERROR)
            }
        }

    fun tick(client: MinecraftClient) {
        if (!initialized) {
            initialize(client)
        }

        musicManager!!.tick()
    }

    fun playSoundNow(sound: PlayableSound?, keepBackground: Boolean = false) {
        musicManager?.playNow(sound, keepBackground)
    }

    fun refreshCurrentMusicPack() {
        musicPack = musicPack
    }

    fun hasActiveEvent(eventType: String): Boolean {
        return musicManager?.hasActiveEvent(eventType) ?: false
    }

    fun getPlayingEvent(): MusicEvent? {
        return musicManager?.playingEvent
    }

    private fun initialize(client: MinecraftClient) {
        if (initialized) {
            return
        }

        musicManager = MusicManager(client)

        options =
            try {
                TrueAdaptiveMusicOptions.fromJson(
                    JsonHelper.deserialize(Path(Constants.OPTIONS_FILENAME).toFile().reader()))
            }
            catch (_: Exception) {
                TrueAdaptiveMusicOptions()
            }

        try {
            musicPack =
                if (options.selectedPack.isBlank())
                    null
                else
                    MusicPack.fromFile(Path(Constants.MUSIC_PACK_DIR, options.selectedPack))
        }
        catch (e: MusicLoadException) {
            log(
                "Selected pack \"${options.selectedPack}\" failed to load. Error:\n$e",
                LogLevel.ERROR)
        }

        options.save()

        initialized = true
    }
}