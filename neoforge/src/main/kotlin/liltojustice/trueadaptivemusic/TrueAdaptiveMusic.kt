package liltojustice.trueadaptivemusic

import liltojustice.trueadaptivemusic.client.NeoforgeModReflectionInterface
import liltojustice.trueadaptivemusic.client.network.NeoforgeClientNetworkingInterface
import liltojustice.trueadaptivemusic.common.TAMMainInitializer
import liltojustice.trueadaptivemusic.common.client.TAMClientInitializer
import liltojustice.trueadaptivemusic.network.NeoforgeServerNetworkingInterface
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.runForDist


@Mod("trueadaptivemusic")
object TrueAdaptiveMusic {
    init {
        runForDist(
            clientTarget = {
                MOD_BUS.addListener(::onClientSetup)
            }, serverTarget = {
                MOD_BUS.addListener(::onServerSetup)
            }
        )
    }

    private fun onClientSetup(@Suppress("UNUSED") event: FMLClientSetupEvent) {
        TAMMainInitializer.onInitialize(NeoforgeServerNetworkingInterface)
        TAMClientInitializer.onInitializeClient(
            NeoforgeClientNetworkingInterface, NeoforgeModReflectionInterface)
    }

    private fun onServerSetup(@Suppress("UNUSED") event: FMLDedicatedServerSetupEvent) {
        TAMMainInitializer.onInitialize(NeoforgeServerNetworkingInterface)
    }
}
