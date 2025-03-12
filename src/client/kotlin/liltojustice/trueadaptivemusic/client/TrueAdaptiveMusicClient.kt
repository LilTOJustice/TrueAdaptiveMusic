package liltojustice.trueadaptivemusic.client

import kotlinx.io.files.FileNotFoundException
import liltojustice.trueadaptivemusic.*
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.util.ActionResult
import kotlin.io.path.Path

class TrueAdaptiveMusicClient: ClientModInitializer {
    override fun onInitializeClient() {
        var musicManager: MusicManager? = null

        ChangeMusicPackCallback.EVENT.register { musicPack ->
            try {
                musicManager?.selectMusicPack(musicPack)
            } catch (_: FileNotFoundException) {
                return@register ActionResult.FAIL
            }

            return@register ActionResult.PASS
        }

        GetMusicPackCallback.EVENT.register { packResult ->
            packResult[0] = musicManager?.getMusicPack()

            return@register ActionResult.PASS
        }

        PlaySoundNowCallback.EVENT.register { sound ->
            musicManager?.playNow(sound)

            return@register ActionResult.PASS
        }

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            // Music manager needs to be initialized here otherwise the client soundManager won't be initialized yet
            if (musicManager == null)
            {
                musicManager = MusicManager(client)
                try {
                    val selectedPackName = Path(Constants.SELECTED_PACK).toFile().readText()

                    try {
                        Callbacks.setCurrentMusicPack(
                            if (selectedPackName.isBlank())
                                null
                            else
                                MusicPack.fromFile(Path(Constants.MUSIC_PACK_DIR, selectedPackName)))
                    }
                    catch (e: MusicLoadException) {
                        Logger.log(
                            "Selected pack \"$selectedPackName\" failed to load. Error:\n${e}")
                    }
                }
                catch (e: FileNotFoundException) {
                    Logger.log("Couldn't find selected music pack. Error:\n${e.message}.", LogLevel.ERROR)
                }
            }

            musicManager!!.tick()
        }
    }
}
