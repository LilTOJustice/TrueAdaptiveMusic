package liltojustice.trueadaptivemusic

import liltojustice.trueadaptivemusic.network.model.CurrentStructurePayload
import liltojustice.trueadaptivemusic.network.model.CurrentStructureSetPayload
import liltojustice.trueadaptivemusic.network.model.ServerStateProcessor
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TrueAdaptiveMusic: ModInitializer {
    override fun onInitialize() {
        PayloadTypeRegistry.clientboundPlay().register(
            CurrentStructurePayload.TYPE, CurrentStructurePayload.CODEC)
        PayloadTypeRegistry.clientboundPlay().register(
            CurrentStructureSetPayload.TYPE, CurrentStructureSetPayload.CODEC)

        ServerTickEvents.END_SERVER_TICK.register { server -> ServerStateProcessor.processServer(server) }
    }

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(TrueAdaptiveMusic::class.java)
    }
}
