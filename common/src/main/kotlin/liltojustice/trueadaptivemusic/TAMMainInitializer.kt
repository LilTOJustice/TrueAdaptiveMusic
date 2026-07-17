package liltojustice.trueadaptivemusic

import liltojustice.trueadaptivemusic.network.ServerNetworkInterface
import liltojustice.trueadaptivemusic.network.TAMServerNetworking

object TAMMainInitializer {
    @Suppress("UNUSED")
    fun onInitialize(serverNetworkInterface: ServerNetworkInterface) {
        TAMServerNetworking.init(serverNetworkInterface)
    }
}
