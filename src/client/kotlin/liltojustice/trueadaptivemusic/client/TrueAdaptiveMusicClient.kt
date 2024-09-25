package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.ChangeMusicPackCallback
import liltojustice.trueadaptivemusic.Constants
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.util.ActionResult
import kotlin.io.path.Path

class TrueAdaptiveMusicClient : ClientModInitializer {
    override fun onInitializeClient() {
        var musicManager: MusicManager? = null

        ChangeMusicPackCallback.EVENT.register { packPath ->
            try {
                musicManager?.loadSoundPack(packPath)
            }
            catch (_: Exception) {
                return@register ActionResult.FAIL
            }

            return@register ActionResult.PASS
        }

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (musicManager == null)
            {
                musicManager = MusicManager(client)
                try {
                    // TODO: Make this look at Constants.SELECTED_PACK_DIR once UI for selecting pack is implemented
                    ChangeMusicPackCallback.EVENT.invoker()
                        .loadPack(Path(Constants.MUSIC_PACK_DIR).toFile().listFiles()!!.first()!!.toPath())
                }
                catch (_: Exception) {}
            }

            musicManager!!.tick()
        }
    }
}
