package liltojustice.trueadaptivemusic.network

import com.google.gson.JsonParser
import liltojustice.trueadaptivemusic.Constants.NULL_IDENTIFIER
import liltojustice.trueadaptivemusic.network.model.CustomPacketPayload
import liltojustice.trueadaptivemusic.network.model.CustomPacketPayloadType
import liltojustice.trueadaptivemusic.network.model.CustomPredicateQueryPayloadType
import liltojustice.trueadaptivemusic.network.model.CustomPredicateResponsePayloadType
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.LootDataType
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import kotlin.jvm.optionals.getOrNull

object TAMServerNetworking {
    private val endTickEvents = mutableListOf<(MinecraftServer) -> Unit>()
    private var networkInterface: ServerNetworkInterface? = null
    fun init(serverNetworkInterface: ServerNetworkInterface) {
        networkInterface = serverNetworkInterface
        serverNetworkInterface.registerServerboundPacket(CustomPredicateQueryPayloadType, { payload, context ->
            val player = context.player as ServerPlayer
            val json = JsonParser.parseString(payload.predicateText)
            val condition = LootDataType.PREDICATE.deserialize(NULL_IDENTIFIER, json, ResourceManager.Empty.INSTANCE)
                .getOrNull()
                ?: return@registerServerboundPacket
            serverNetworkInterface.sendToClient(
                player,
                CustomPredicateResponsePayloadType,
                CustomPredicateResponsePayloadType.CustomPredicateResponsePayload(
                    payload.predicateId,
                    condition.test(
                        LootContext.Builder(
                            LootParams.Builder(player.serverLevel())
                                .withOptionalParameter(LootContextParams.ORIGIN, player.position())
                                .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
                                .create(LootContextParamSets.COMMAND)
                        ).create(null)
                    )
                )
            )
        })

        endTickEvents.add { server -> ServerStateProcessor.processServer(server, serverNetworkInterface) }
    }

    fun <T: CustomPacketPayload> sendToClient(player: ServerPlayer, type: CustomPacketPayloadType<T>, payload: T) {
        networkInterface?.sendToClient(player, type, payload)
            ?: throw TrueAdaptiveMusicNetworkingException("TAM server network interface was not initialized!")
    }

    @Suppress("UNUSED")
    fun processTick(server: MinecraftServer) {
        endTickEvents.forEach { it(server) }
    }
}