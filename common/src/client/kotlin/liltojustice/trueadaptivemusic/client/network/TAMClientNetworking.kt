package liltojustice.trueadaptivemusic.client.network

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.network.ClientNetworkInterface
import liltojustice.trueadaptivemusic.network.TrueAdaptiveMusicNetworkingException
import liltojustice.trueadaptivemusic.network.model.CurrentStructurePayloadType
import liltojustice.trueadaptivemusic.network.model.CustomPredicateQueryPayloadType
import liltojustice.trueadaptivemusic.network.model.CustomPredicateResponsePayloadType
import liltojustice.trueadaptivemusic.network.model.ScoreboardStatePayloadType
import liltojustice.trueadaptivemusic.network.model.SpawnPoint
import liltojustice.trueadaptivemusic.network.model.SpawnPointPayloadType
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
        clientNetworkInterface.registerClientboundPacket(CurrentStructurePayloadType) { payload, _ ->
            structureId = payload.structureIdentifier
            structureSetId = payload.structureSetIdentifier
            structurePieceId = payload.structurePieceIdentifier
        }
        clientNetworkInterface.registerClientboundPacket(SpawnPointPayloadType) { payload, _ ->
            spawnPoint = payload.spawnPoint
        }
        clientNetworkInterface.registerClientboundPacket(CustomPredicateResponsePayloadType) { payload, _ ->
            customPredicateResults[payload.predicateId] = payload.predicateResponse
        }
        clientNetworkInterface.registerClientboundPacket(ScoreboardStatePayloadType) { payload, _ ->
            scoreboardState[payload.objectiveName] = payload.value
        }
    }

    fun queryCustomPredicate(predicateId: String, predicateString: String): Boolean {
        networkInterface?.sendToServer(
            CustomPredicateQueryPayloadType,
            CustomPredicateQueryPayloadType.CustomPredicateQueryPayload(
                predicateId, predicateString
            )
        ) ?: throw TrueAdaptiveMusicNetworkingException("TAM client network interface was not initialized!")

        return customPredicateResults[predicateId] ?: false
    }
}