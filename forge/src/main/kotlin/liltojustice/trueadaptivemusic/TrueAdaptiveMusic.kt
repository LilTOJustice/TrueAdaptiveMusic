package liltojustice.trueadaptivemusic

import liltojustice.trueadaptivemusic.client.ForgeModReflectionInterface
import liltojustice.trueadaptivemusic.client.TAMClientInitializer
import liltojustice.trueadaptivemusic.client.network.ForgeClientNetworkingInterface
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.runForDist
import liltojustice.trueadaptivemusic.network.ForgeServerNetworkingInterface
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent


@Mod("trueadaptivemusic")
@Mod.EventBusSubscriber
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
            ForgeClientNetworkingInterface,
            ForgeModReflectionInterface
        )
    }

    @SubscribeEvent
    fun onCommonSetup(@Suppress("UNUSED") event: FMLCommonSetupEvent) {
        TAMMainInitializer.onInitialize(ForgeServerNetworkingInterface)
    }

    fun onServerSetup(@Suppress("UNUSED") event: FMLDedicatedServerSetupEvent) {
    }
}
