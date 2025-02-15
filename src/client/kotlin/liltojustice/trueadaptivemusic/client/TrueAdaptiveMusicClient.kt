package liltojustice.trueadaptivemusic.client

import kotlinx.io.files.FileNotFoundException
import liltojustice.trueadaptivemusic.*
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.util.ActionResult
import java.nio.file.Path
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

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            // Music manager needs to be initialized here otherwise the client soundManager won't be initialized yet
            if (musicManager == null)
            {
                musicManager = MusicManager(client)
                try {
                    var toLoad: Path? = null
                    val selectedPackFileText = Path(Constants.SELECTED_PACK).toFile().readText()
                    val selectedPackPath = Path("${Constants.MUSIC_PACK_DIR}/${selectedPackFileText}")
                    if (selectedPackFileText.isNotEmpty()) {
                        toLoad = selectedPackPath
                        Logger.log("Found selected pack $selectedPackPath.")
                    }

                    if (toLoad != null)
                    {
                        try {
                            ChangeMusicPackCallback.EVENT.invoker().selectPack(MusicPack.fromFile(toLoad))
                        }
                        catch (e: MusicLoadException) {
                            Logger.log(
                                "Selected pack \"$selectedPackPath\" failed to load. Error:\n${e}")
                        }
                    }
                    else {
                        Logger.log("Selected pack \"$selectedPackPath\" is missing.", LogLevel.WARNING)
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
