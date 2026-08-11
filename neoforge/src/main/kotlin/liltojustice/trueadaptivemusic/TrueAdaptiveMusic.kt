package liltojustice.trueadaptivemusic

import liltojustice.trueadaptivemusic.client.NeoforgeModReflectionInterface
import liltojustice.trueadaptivemusic.client.TAMClientInitializer
import liltojustice.trueadaptivemusic.client.gui.ConfigScreenFactory
import liltojustice.trueadaptivemusic.client.network.NeoforgeClientNetworkingInterface
import liltojustice.trueadaptivemusic.network.NeoforgeServerNetworkingInterface
import net.minecraft.client.Minecraft
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent
import net.neoforged.neoforge.client.gui.IConfigScreenFactory
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.runForDist


@Mod("trueadaptivemusic")
@EventBusSubscriber
object TrueAdaptiveMusic {
    init {
        runForDist(
            clientTarget = {
                MOD_BUS.addListener(::onClientSetup)
                Minecraft.getInstance()
            }, serverTarget = {
                MOD_BUS.addListener(::onServerSetup)
                "test"
            }
        )
    }

    private fun onClientSetup(@Suppress("UNUSED") event: FMLClientSetupEvent) {
        TAMClientInitializer.onInitializeClient(
            NeoforgeClientNetworkingInterface,
            NeoforgeModReflectionInterface
        )

        event.container.registerExtensionPoint(IConfigScreenFactory::class.java, ConfigScreenFactory)
    }

    @SubscribeEvent
    private fun onCommonSetup(@Suppress("UNUSED") event: FMLCommonSetupEvent) {
        TAMMainInitializer.onInitialize(NeoforgeServerNetworkingInterface)
    }

    private fun onServerSetup(@Suppress("UNUSED") event: FMLDedicatedServerSetupEvent) {
    }
}
