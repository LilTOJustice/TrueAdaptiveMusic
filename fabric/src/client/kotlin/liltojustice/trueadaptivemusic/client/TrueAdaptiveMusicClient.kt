package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.client.network.FabricClientNetworkInterface
import net.fabricmc.api.ClientModInitializer

class TrueAdaptiveMusicClient: ClientModInitializer {
    override fun onInitializeClient() {
        TAMClientInitializer.onInitializeClient(FabricClientNetworkInterface, FabricClientModReflectionInterface)
    }
}