package liltojustice.trueadaptivemusic.network

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
    private var networkInterface: ServerNetworkInterface? = null
    fun init(serverNetworkInterface: ServerNetworkInterface) {
        networkInterface = serverNetworkInterface
        registerClientbound()
        registerServerbound()

        endTickEvents.add { server -> ServerStateProcessor.processServer(server, serverNetworkInterface) }
    }

    fun sendToClient(player: ServerPlayer, payload: ScoreboardStatePayload) {
        networkInterface?.sendToClient(player, payload)
            ?: throw TrueAdaptiveMusicNetworkingException("TAM server network interface was not initialized!")
    }

    @Suppress("UNUSED")
    fun processTick(server: MinecraftServer) {
        endTickEvents.forEach { it(server) }
    }

    private fun registerClientbound() {
        networkInterface?.let {
            it.registerClientboundPacket(CurrentStructurePayload.TYPE, CurrentStructurePayload.CODEC)
            it.registerClientboundPacket(SpawnPointPayload.TYPE, SpawnPointPayload.CODEC)
            it.registerClientboundPacket(CustomPredicateResponsePayload.TYPE, CustomPredicateResponsePayload.CODEC)
            it.registerClientboundPacket(ScoreboardStatePayload.TYPE, ScoreboardStatePayload.CODEC)
        } ?: throw TrueAdaptiveMusicNetworkingException("TAM server network interface was not initialized!")
    }

    private fun registerServerbound() {
        networkInterface?.let {
            it.registerServerboundPacket(
                CustomPredicateQueryPayload.TYPE, CustomPredicateQueryPayload.CODEC) { payload, context ->
                val player = context.player as ServerPlayer
                val json = StrictJsonParser.parse(payload.predicateText)
                val condition = LootItemCondition.CODEC.parse(Dynamic(JsonOps.INSTANCE, json))
                    .result()
                    .getOrNull()
                    ?.value() ?: return@registerServerboundPacket
                it.sendToClient(
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
        }
    }
}