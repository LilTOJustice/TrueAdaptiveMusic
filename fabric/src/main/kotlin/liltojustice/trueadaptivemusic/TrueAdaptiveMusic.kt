package liltojustice.trueadaptivemusic

import liltojustice.trueadaptivemusic.network.FabricServerNetworkInterface
import net.fabricmc.api.ModInitializer

class TrueAdaptiveMusic: ModInitializer {
    override fun onInitialize() {
        TAMMainInitializer.onInitialize(FabricServerNetworkInterface)
    }
}
