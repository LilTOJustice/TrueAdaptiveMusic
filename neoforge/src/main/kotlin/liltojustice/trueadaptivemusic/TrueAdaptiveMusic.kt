package liltojustice.trueadaptivemusic

import liltojustice.trueadaptivemusic.common.client.TrueAdaptiveMusicClientInitializer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.runForDist

@Mod("trueadaptivemusic")
object TrueAdaptiveMusic {
    init {
        runForDist(clientTarget = {
            MOD_BUS.addListener(::onClientSetup)
        }, serverTarget = {
            MOD_BUS.addListener(::onServerSetup)
        })
    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        TrueAdaptiveMusicClientInitializer.onInitializeClient()
    }

    private fun onServerSetup(event: FMLDedicatedServerSetupEvent) {
    }
}
