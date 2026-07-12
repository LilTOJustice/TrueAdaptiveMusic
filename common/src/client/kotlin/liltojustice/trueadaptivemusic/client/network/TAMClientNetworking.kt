package liltojustice.trueadaptivemusic.client.network

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.network.ClientNetworkInterface
import liltojustice.trueadaptivemusic.network.TrueAdaptiveMusicNetworkingException
import liltojustice.trueadaptivemusic.network.model.CurrentStructurePayload
import liltojustice.trueadaptivemusic.network.model.CustomPredicateQueryPayload
import liltojustice.trueadaptivemusic.network.model.CustomPredicateResponsePayload
import liltojustice.trueadaptivemusic.network.model.ScoreboardStatePayload
import liltojustice.trueadaptivemusic.network.model.SpawnPointPayload
import net.minecraft.resources.Identifier
import net.minecraft.world.level.storage.LevelData

object TAMClientNetworking {
    var structureId: Identifier = Constants.NULL_IDENTIFIER
        private set
    var structureSetId: Identifier = Constants.NULL_IDENTIFIER
        private set
    var structurePieceId: Identifier = Constants.NULL_IDENTIFIER
        private set
    var spawnPoint: LevelData.RespawnData? = null
        private set
    val customPredicateResults = mutableMapOf<String, Boolean>()
    val scoreboardState = mutableMapOf<String, Int>()

    private var networkInterface: ClientNetworkInterface? = null

    fun init(clientNetworkInterface: ClientNetworkInterface) {
        networkInterface = clientNetworkInterface
        clientNetworkInterface.registerClientboundPacket(
            CurrentStructurePayload.TYPE, CurrentStructurePayload.CODEC) { payload, _ ->
            structureId = payload.structureIdentifier
            structureSetId = payload.structureSetIdentifier
            structurePieceId = payload.structurePieceIdentifier
        }
        clientNetworkInterface.registerClientboundPacket(SpawnPointPayload.TYPE, SpawnPointPayload.CODEC) { payload, _ ->
            spawnPoint = payload.spawnPoint
        }
        clientNetworkInterface.registerClientboundPacket(
            CustomPredicateResponsePayload.TYPE, CustomPredicateResponsePayload.CODEC) { payload, _ ->
            customPredicateResults[payload.predicateId] = payload.predicateResponse
        }
        clientNetworkInterface.registerClientboundPacket(
            ScoreboardStatePayload.TYPE, ScoreboardStatePayload.CODEC) { payload, _ ->
            scoreboardState[payload.objectiveName] = payload.value
        }
    }

    fun queryCustomPredicate(predicateId: String, predicateString: String): Boolean {
        networkInterface?.sendToServer(CustomPredicateQueryPayload(predicateId, predicateString))
            ?: throw TrueAdaptiveMusicNetworkingException("TAM client network interface was not initialized!")

        return customPredicateResults[predicateId] ?: false
    }
}