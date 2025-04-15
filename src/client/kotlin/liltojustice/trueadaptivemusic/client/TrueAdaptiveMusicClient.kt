package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.LogLevel
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.util.ActionResult
import kotlin.io.path.Path

class TrueAdaptiveMusicClient: ClientModInitializer {
    override fun onInitializeClient() {
        var musicManager: MusicManager? = null

        GetMusicManagerCallback.EVENT.register { managerResult ->
            managerResult[0] = musicManager

            return@register ActionResult.PASS
        }

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            // Music manager needs to be initialized here otherwise the client soundManager won't be initialized yet
            if (musicManager == null)
            {
                musicManager = MusicManager(client)
                val selectedPackName = Path(Constants.SELECTED_PACK).toFile().readText()

                try {
                    setCurrentMusicPack(
                        if (selectedPackName.isBlank())
                            null
                        else
                            MusicPack.fromFile(Path(Constants.MUSIC_PACK_DIR, selectedPackName)))
                }
                catch (e: MusicLoadException) {
                    Logger.log(
                        "Selected pack \"$selectedPackName\" failed to load. Error:\n$e",
                        LogLevel.ERROR)
                }
            }

            musicManager!!.tick()
        }
    }

    companion object {
        fun getMusicManager(): MusicManager? {
            val result = Array<MusicManager?>(1) { null }
            GetMusicManagerCallback.EVENT.invoker().getMusicManager(result)
            return result[0]
        }

        fun getCurrentMusicPack(): MusicPack? {
            return getMusicManager()?.getMusicPack()
        }

        fun setCurrentMusicPack(musicPack: MusicPack?) {
            ChangeMusicPackCallback.EVENT.invoker().selectPack(musicPack)
            getMusicManager()?.selectMusicPack(musicPack)
        }

        fun refreshCurrentMusicPack() {
            setCurrentMusicPack(getCurrentMusicPack())
        }

        fun playSoundNow(sound: PlayableSound?, keepBackground: Boolean = false) {
            getMusicManager()?.playNow(sound, keepBackground)
        }

        fun eventActive(eventName: String): Boolean {
            return getMusicManager()?.hasActiveEvent(eventName) ?: false
        }
    }
}
