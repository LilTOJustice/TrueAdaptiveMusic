package liltojustice.trueadaptivemusic

import com.google.gson.JsonParser
import liltojustice.trueadaptivemusic.Constants.Companion.NULL_IDENTIFIER
import liltojustice.trueadaptivemusic.network.ServerStateProcessor
import liltojustice.trueadaptivemusic.network.model.CustomPredicateQueryPayload
import liltojustice.trueadaptivemusic.network.model.CustomPredicateResponsePayload
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.loot.LootDataType
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.loot.context.LootContextTypes
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.jvm.optionals.getOrNull

class TrueAdaptiveMusic: ModInitializer {
    override fun onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(CustomPredicateQueryPayload.TYPE) { packet, player, _ ->
            val json = JsonParser.parseString(packet.predicateText)
            val condition = LootDataType.PREDICATES.parse(NULL_IDENTIFIER, json)
                .getOrNull()
                ?: return@registerGlobalReceiver
            ServerPlayNetworking.send(
                player,
                CustomPredicateResponsePayload(
                    packet.predicateId,
                    condition.test(
                        LootContext.Builder(
                            LootContextParameterSet.Builder(player.serverWorld)
                                .addOptional(LootContextParameters.ORIGIN, player.pos)
                                .addOptional(LootContextParameters.THIS_ENTITY, player)
                                .build(LootContextTypes.COMMAND)
                        ).build(null)
                    )
                )
            )
        }
        ServerStateProcessor().let { ServerTickEvents.END_SERVER_TICK.register { server -> it.processServer(server) } }
    }

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(TrueAdaptiveMusic::class.java)
    }
}
