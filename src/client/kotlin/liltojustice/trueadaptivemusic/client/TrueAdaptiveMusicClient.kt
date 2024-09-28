package liltojustice.trueadaptivemusic.client

import kotlinx.io.files.FileNotFoundException
import liltojustice.trueadaptivemusic.ChangeMusicPackCallback
import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.LogLevel
import liltojustice.trueadaptivemusic.Logger
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.util.ActionResult
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists

class TrueAdaptiveMusicClient : ClientModInitializer {
    override fun onInitializeClient() {
        var musicManager: MusicManager? = null

        ChangeMusicPackCallback.EVENT.register { packPath ->
            try {
                musicManager?.loadSoundPack(packPath)
            } catch (_: FileNotFoundException) {
                return@register ActionResult.FAIL
            }

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
                    val firstPackPath = Path(Constants.MUSIC_PACK_DIR).toFile().listFiles()?.first()?.toPath()
                    if (selectedPackFileText.isNotEmpty()) {
                        toLoad = selectedPackPath
                        Logger.log("Found selected pack $selectedPackPath.")
                    } else if (firstPackPath?.exists() == true) {
                        toLoad = firstPackPath
                        Logger.log("No selected pack found. Defaulting to $firstPackPath.", LogLevel.WARNING)
                    }

                    ChangeMusicPackCallback.EVENT.invoker().loadPack(toLoad)
                } catch (e: FileNotFoundException) {
                    Logger.log("Couldn't find selected music pack. Error:\n${e.message}.", LogLevel.ERROR)
                }
            }

            try {
                musicManager!!.tick()
            } catch (e: MusicLoadException) {
                Logger.log("Failed to load music. Error:\n${e.message}.", LogLevel.ERROR)
            }
        }
    }
}
