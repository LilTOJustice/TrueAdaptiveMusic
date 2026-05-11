package liltojustice.trueadaptivemusic

import com.mojang.serialization.Dynamic
import com.mojang.serialization.JsonOps
import liltojustice.trueadaptivemusic.network.model.CurrentStructurePayload
import liltojustice.trueadaptivemusic.network.ServerStateProcessor
import liltojustice.trueadaptivemusic.network.model.CustomPredicateQueryPayload
import liltojustice.trueadaptivemusic.network.model.CustomPredicateResponsePayload
import liltojustice.trueadaptivemusic.network.model.ScoreboardStatePayload
import liltojustice.trueadaptivemusic.network.model.SpawnPointPayload
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.loot.condition.LootCondition
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.loot.context.LootContextTypes
import net.minecraft.loot.context.LootWorldContext
import net.minecraft.util.StrictJsonParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

class TrueAdaptiveMusic: ModInitializer {
    override fun onInitialize() {
        PayloadTypeRegistry.playS2C().register(
            CurrentStructurePayload.ID, CurrentStructurePayload.CODEC)
        PayloadTypeRegistry.playS2C().register(
            SpawnPointPayload.ID, SpawnPointPayload.CODEC)
        PayloadTypeRegistry.playC2S().register(
            CustomPredicateQueryPayload.ID, CustomPredicateQueryPayload.CODEC)
        PayloadTypeRegistry.playS2C().register(
            CustomPredicateResponsePayload.ID, CustomPredicateResponsePayload.CODEC)
        PayloadTypeRegistry.playS2C().register(
            ScoreboardStatePayload.ID, ScoreboardStatePayload.CODEC)
        ServerPlayNetworking.registerGlobalReceiver(CustomPredicateQueryPayload.ID) { payload, context ->
            val player = context.player()
            val json = StrictJsonParser.parse(payload.predicateText)
            val condition = LootCondition.CODEC.parse(Dynamic(JsonOps.INSTANCE, json))
                .result()
                .getOrNull()
                ?: return@registerGlobalReceiver
            ServerPlayNetworking.send(
                player,
                CustomPredicateResponsePayload(
                    payload.predicateId,
                    condition.test(
                        LootContext.Builder(
                            LootWorldContext.Builder(player.world)
                                .addOptional(LootContextParameters.ORIGIN, player.pos)
                                .addOptional(LootContextParameters.THIS_ENTITY, player)
                                .build(LootContextTypes.COMMAND)
                        ).build(Optional.empty())
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
