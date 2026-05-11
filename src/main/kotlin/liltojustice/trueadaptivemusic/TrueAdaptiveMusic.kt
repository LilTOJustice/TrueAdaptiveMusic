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
import net.minecraft.util.StrictJsonParser
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

class TrueAdaptiveMusic: ModInitializer {
    override fun onInitialize() {
        PayloadTypeRegistry.clientboundPlay().register(
            CurrentStructurePayload.TYPE, CurrentStructurePayload.CODEC)
        PayloadTypeRegistry.clientboundPlay().register(
            SpawnPointPayload.TYPE, SpawnPointPayload.CODEC)
        PayloadTypeRegistry.serverboundPlay().register(
            CustomPredicateQueryPayload.TYPE, CustomPredicateQueryPayload.CODEC)
        PayloadTypeRegistry.clientboundPlay().register(
            CustomPredicateResponsePayload.TYPE, CustomPredicateResponsePayload.CODEC)
        PayloadTypeRegistry.clientboundPlay().register(
            ScoreboardStatePayload.TYPE, ScoreboardStatePayload.CODEC)
        ServerPlayNetworking.registerGlobalReceiver(CustomPredicateQueryPayload.TYPE) { payload, context ->
            val player = context.player()
            val json = StrictJsonParser.parse(payload.predicateText)
            val condition = LootItemCondition.CODEC.parse(Dynamic(JsonOps.INSTANCE, json))
                .result()
                .getOrNull()
                ?.value() ?: return@registerGlobalReceiver
            ServerPlayNetworking.send(
                player,
                CustomPredicateResponsePayload(
                    payload.predicateId,
                    condition.test(
                        LootContext.Builder(
                            LootParams.Builder(player.level())
                                .withParameter(LootContextParams.ORIGIN, player.position())
                                .withOptionalParameter(
                                    LootContextParams.THIS_ENTITY, player.livingEntity)
                                .create(LootContextParamSets.COMMAND)
                        ).create(Optional.empty())
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
