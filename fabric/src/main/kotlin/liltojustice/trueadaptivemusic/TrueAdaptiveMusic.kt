package liltojustice.trueadaptivemusic

import liltojustice.trueadaptivemusic.network.FabricServerNetworkInterface
import liltojustice.trueadaptivemusic.network.TAMServerNetworking
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents

class TrueAdaptiveMusic: ModInitializer {
    override fun onInitialize() {
        TAMMainInitializer.onInitialize(FabricServerNetworkInterface)
        ServerTickEvents.END_SERVER_TICK.register(TAMServerNetworking::processTick)
    }
}
