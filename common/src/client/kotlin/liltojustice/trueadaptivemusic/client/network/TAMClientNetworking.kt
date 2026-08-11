package liltojustice.trueadaptivemusic.client.network

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.network.ClientNetworkInterface
import liltojustice.trueadaptivemusic.network.TrueAdaptiveMusicNetworkingException
import liltojustice.trueadaptivemusic.network.model.CurrentStructurePayload
import liltojustice.trueadaptivemusic.network.model.CustomPredicateQueryPayload
import liltojustice.trueadaptivemusic.network.model.CustomPredicateResponsePayload
import liltojustice.trueadaptivemusic.network.model.ScoreboardStatePayload
import liltojustice.trueadaptivemusic.network.model.SpawnPointPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.LevelData

object TAMClientNetworking {
    var structureId: ResourceLocation = Constants.NULL_IDENTIFIER
    var structureSetId: ResourceLocation = Constants.NULL_IDENTIFIER
    var structurePieceId: ResourceLocation = Constants.NULL_IDENTIFIER
    var spawnPoint: LevelData.RespawnData? = null
    val customPredicateResults = mutableMapOf<String, Boolean>()
    val scoreboardState = mutableMapOf<String, Int>()
    var networkInterface: ClientNetworkInterface? = null

    private val network
        get() = networkInterface
            ?: throw TrueAdaptiveMusicNetworkingException("TAM client network interface was not initialized!")

    fun init(clientNetworkInterface: ClientNetworkInterface) {
        networkInterface = clientNetworkInterface
        registerClientbound()
        registerServerbound()
    }

    private fun registerClientbound() {
        network.registerClientboundPacket(
            CurrentStructurePayload.TYPE, CurrentStructurePayload.CODEC) { payload, _ ->
            structureId = payload.structureIdentifier
            structureSetId = payload.structureSetIdentifier
            structurePieceId = payload.structurePieceIdentifier
        }
        network.registerClientboundPacket(SpawnPointPayload.TYPE, SpawnPointPayload.CODEC) { payload, _ ->
            spawnPoint = payload.spawnPoint
        }
        network.registerClientboundPacket(
            CustomPredicateResponsePayload.TYPE, CustomPredicateResponsePayload.CODEC) { payload, _ ->
            customPredicateResults[payload.predicateId] = payload.predicateResponse
        }
        network.registerClientboundPacket(
            ScoreboardStatePayload.TYPE, ScoreboardStatePayload.CODEC) { payload, _ ->
            scoreboardState[payload.objectiveName] = payload.value
        }
    }

    fun registerServerbound() {
        network.registerServerboundPacket(CustomPredicateQueryPayload.TYPE, CustomPredicateQueryPayload.CODEC)
    }

    fun queryCustomPredicate(predicateId: String, predicateString: String): Boolean {
        network.sendToServer(CustomPredicateQueryPayload(predicateId, predicateString))

        return customPredicateResults[predicateId] ?: false
    }
}