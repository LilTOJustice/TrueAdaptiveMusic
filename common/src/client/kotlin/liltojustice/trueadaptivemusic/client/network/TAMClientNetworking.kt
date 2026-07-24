package liltojustice.trueadaptivemusic.client.network

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.network.ClientNetworkInterface
import liltojustice.trueadaptivemusic.network.TrueAdaptiveMusicNetworkingException
import liltojustice.trueadaptivemusic.network.model.CurrentStructurePayload
import liltojustice.trueadaptivemusic.network.model.CustomPredicateQueryPayload
import liltojustice.trueadaptivemusic.network.model.CustomPredicateResponsePayload
import liltojustice.trueadaptivemusic.network.model.ScoreboardStatePayload
import liltojustice.trueadaptivemusic.network.model.SpawnPoint
import liltojustice.trueadaptivemusic.network.model.SpawnPointPayload
import net.minecraft.resources.ResourceLocation

object TAMClientNetworking {
    var structureId: ResourceLocation = Constants.NULL_IDENTIFIER
    var structureSetId: ResourceLocation = Constants.NULL_IDENTIFIER
    var structurePieceId: ResourceLocation = Constants.NULL_IDENTIFIER
    var spawnPoint: SpawnPoint? = null
    val customPredicateResults = mutableMapOf<String, Boolean>()
    val scoreboardState = mutableMapOf<String, Int>()
    var networkInterface: ClientNetworkInterface? = null

    fun init(clientNetworkInterface: ClientNetworkInterface) {
        networkInterface = clientNetworkInterface
        clientNetworkInterface.registerClientboundPacket(
            CurrentStructurePayload.ID, CurrentStructurePayload.CODEC) { payload, _ ->
            structureId = payload.structureIdentifier
            structureSetId = payload.structureSetIdentifier
            structurePieceId = payload.structurePieceIdentifier
        }
        clientNetworkInterface.registerClientboundPacket(SpawnPointPayload.ID, SpawnPointPayload.CODEC) { payload, _ ->
            spawnPoint = payload.spawnPoint
        }
        clientNetworkInterface.registerClientboundPacket(
            CustomPredicateResponsePayload.ID, CustomPredicateResponsePayload.CODEC) { payload, _ ->
            customPredicateResults[payload.predicateId] = payload.predicateResponse
        }
        clientNetworkInterface.registerClientboundPacket(
            ScoreboardStatePayload.ID, ScoreboardStatePayload.CODEC) { payload, _ ->
            scoreboardState[payload.objectiveName] = payload.value
        }
    }

    fun queryCustomPredicate(predicateId: String, predicateString: String): Boolean {
        networkInterface?.sendToServer(CustomPredicateQueryPayload(predicateId, predicateString))
            ?: throw TrueAdaptiveMusicNetworkingException("TAM client network interface was not initialized!")

        return customPredicateResults[predicateId] ?: false
    }
}