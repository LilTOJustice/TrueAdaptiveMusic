package liltojustice.trueadaptivemusic.common

import liltojustice.trueadaptivemusic.common.network.ServerNetworkInterface
import liltojustice.trueadaptivemusic.common.network.TAMServerNetworking

object TAMMainInitializer {
    @Suppress("UNUSED")
    fun onInitialize(serverNetworkInterface: ServerNetworkInterface) {
        TAMServerNetworking.init(serverNetworkInterface)
    }
}
