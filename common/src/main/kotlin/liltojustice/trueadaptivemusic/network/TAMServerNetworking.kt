package liltojustice.trueadaptivemusic.network

import com.google.gson.JsonParser
import com.mojang.serialization.Dynamic
import com.mojang.serialization.JsonOps
import liltojustice.trueadaptivemusic.network.model.CurrentStructurePayload
import liltojustice.trueadaptivemusic.network.model.CustomPredicateQueryPayload
import liltojustice.trueadaptivemusic.network.model.CustomPredicateResponsePayload
import liltojustice.trueadaptivemusic.network.model.ScoreboardStatePayload
import liltojustice.trueadaptivemusic.network.model.SpawnPointPayload
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

object TAMServerNetworking {
    private val endTickEvents = mutableListOf<(MinecraftServer) -> Unit>()

    private var networkInterface: ServerNetworkInterface? = null

    private val network
        get() = networkInterface
            ?: throw TrueAdaptiveMusicNetworkingException("TAM server network interface was not initialized!")

    fun init(serverNetworkInterface: ServerNetworkInterface) {
        networkInterface = serverNetworkInterface
        registerClientbound()
        registerServerbound()

        endTickEvents.add { server -> ServerStateProcessor.processServer(server, serverNetworkInterface) }
    }

    fun sendToClient(player: ServerPlayer, payload: CustomPacketPayload) {
        network.sendToClient(player, payload)
    }

    @Suppress("UNUSED")
    fun processTick(server: MinecraftServer) {
        endTickEvents.forEach { it(server) }
    }

    private fun registerClientbound() {
        network.registerClientboundPacket(CurrentStructurePayload.TYPE, CurrentStructurePayload.CODEC)
        network.registerClientboundPacket(SpawnPointPayload.TYPE, SpawnPointPayload.CODEC)
        network.registerClientboundPacket(CustomPredicateResponsePayload.TYPE, CustomPredicateResponsePayload.CODEC)
        network.registerClientboundPacket(ScoreboardStatePayload.TYPE, ScoreboardStatePayload.CODEC)
    }

    private fun registerServerbound() {
        network.registerServerboundPacket(
            CustomPredicateQueryPayload.TYPE, CustomPredicateQueryPayload.CODEC) { payload, context ->
            val player = context.player as ServerPlayer
            val json = JsonParser.parseString(payload.predicateText)
            val condition = LootItemCondition.CODEC.parse(Dynamic(JsonOps.INSTANCE, json))
                .result()
                .getOrNull()
                ?.value() ?: return@registerServerboundPacket
            network.sendToClient(
                player,
                CustomPredicateResponsePayload(
                    payload.predicateId,
                    condition.test(
                        LootContext.Builder(
                            LootParams.Builder(player.serverLevel())
                                .withParameter(LootContextParams.ORIGIN, player.position())
                                .withOptionalParameter(
                                    LootContextParams.THIS_ENTITY, player as LivingEntity
                                )
                                .create(LootContextParamSets.COMMAND)
                        ).create(Optional.empty())
                    )
                )
            )
        }
    }
}