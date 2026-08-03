package liltojustice.trueadaptivemusic

import liltojustice.trueadaptivemusic.client.ForgeModReflectionInterface
import liltojustice.trueadaptivemusic.client.TAMClientInitializer
import liltojustice.trueadaptivemusic.client.network.ForgeClientNetworkInterface
import liltojustice.trueadaptivemusic.client.network.TAMClientNetworking
import liltojustice.trueadaptivemusic.network.FakeClientNetworkInterface
import liltojustice.trueadaptivemusic.network.ForgeServerNetworkInterface
import liltojustice.trueadaptivemusic.network.TAMServerNetworking.processTick
import net.minecraft.client.Minecraft
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.TickEvent.ServerTickEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.runForDist


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

        MOD_BUS.addListener(::onCommonSetup)
        FORGE_BUS.addListener(::onEndServerTick)
    }

    private fun onClientSetup(@Suppress("UNUSED") event: FMLClientSetupEvent) {
        TAMClientInitializer.onInitializeClient(
            ForgeClientNetworkInterface,
            ForgeModReflectionInterface
        )
    }

    fun onCommonSetup(@Suppress("UNUSED") event: FMLCommonSetupEvent) {
        TAMMainInitializer.onInitialize(ForgeServerNetworkInterface)
    }

    fun onEndServerTick(event: ServerTickEvent) {
        if (event.phase == TickEvent.Phase.END) {
            processTick(event.server)
        }
    }

    fun onServerSetup(@Suppress("UNUSED") event: FMLDedicatedServerSetupEvent) {
        TAMClientNetworking.init(FakeClientNetworkInterface)
    }
}
