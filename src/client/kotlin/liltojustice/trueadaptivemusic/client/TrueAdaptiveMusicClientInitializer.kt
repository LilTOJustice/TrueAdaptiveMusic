package liltojustice.trueadaptivemusic.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

class TrueAdaptiveMusicClientInitializer: ClientModInitializer {
    override fun onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            TAMClient.tick(client)
        }
    }
}
