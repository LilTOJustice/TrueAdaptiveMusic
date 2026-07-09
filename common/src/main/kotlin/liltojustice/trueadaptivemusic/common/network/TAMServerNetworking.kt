package liltojustice.trueadaptivemusic.common.network

import com.mojang.serialization.Dynamic
import com.mojang.serialization.JsonOps
import liltojustice.trueadaptivemusic.common.network.model.CustomPredicateQueryPayload
import liltojustice.trueadaptivemusic.common.network.model.CustomPredicateResponsePayload
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.StrictJsonParser
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

object TAMServerNetworking {
    private val endTickEvents = mutableListOf<(MinecraftServer) -> Unit>()
    fun init(serverNetworkInterface: ServerNetworkInterface) {
        serverNetworkInterface.registerServerboundPacket(CustomPredicateQueryPayload.TYPE, CustomPredicateQueryPayload.CODEC) { payload, context ->
            val player = context.player as ServerPlayer
            val json = StrictJsonParser.parse(payload.predicateText)
            val condition = LootItemCondition.CODEC.parse(Dynamic(JsonOps.INSTANCE, json))
                .result()
                .getOrNull()
                ?.value() ?: return@registerServerboundPacket
            serverNetworkInterface.sendToClient(
                player,
                CustomPredicateResponsePayload(
                    payload.predicateId,
                    condition.test(
                        LootContext.Builder(
                            LootParams.Builder(player.level())
                                .withParameter(LootContextParams.ORIGIN, player.position())
                                .withOptionalParameter(
                                    LootContextParams.THIS_ENTITY, player.livingEntity
                                )
                                .create(LootContextParamSets.COMMAND)
                        ).create(Optional.empty())
                    )
                )
            )
        }

        endTickEvents.add { server -> ServerStateProcessor.processServer(server, serverNetworkInterface) }
    }

    @Suppress("UNUSED")
    fun processTick(server: MinecraftServer) {
        endTickEvents.forEach { it(server) }
    }
}