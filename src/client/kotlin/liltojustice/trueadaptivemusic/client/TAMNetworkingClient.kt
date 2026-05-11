package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.Constants.Companion.NULL_IDENTIFIER
import liltojustice.trueadaptivemusic.network.model.CurrentStructurePayload
import liltojustice.trueadaptivemusic.network.model.CustomPredicateQueryPayload
import liltojustice.trueadaptivemusic.network.model.CustomPredicateResponsePayload
import liltojustice.trueadaptivemusic.network.model.SpawnPoint
import liltojustice.trueadaptivemusic.network.model.ScoreboardStatePayload
import liltojustice.trueadaptivemusic.network.model.SpawnPointPayload
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.util.Identifier

object TAMNetworkingClient {
    var structureId: Identifier = NULL_IDENTIFIER
    var structureSetId: Identifier = NULL_IDENTIFIER
    var structurePieceId: Identifier = NULL_IDENTIFIER
    var spawnPoint: SpawnPoint? = null
    val customPredicateResults = mutableMapOf<String, Boolean>()
    val scoreboardState = mutableMapOf<String, Int>()

    fun init() {
        ClientPlayNetworking.registerGlobalReceiver(CurrentStructurePayload.TYPE) { packet, _, _ ->
            structureId = packet.structureIdentifier
            structureSetId = packet.structureSetIdentifier
            structurePieceId = packet.structurePieceIdentifier
        }
        ClientPlayNetworking.registerGlobalReceiver(SpawnPointPayload.TYPE) { packet, _, _ ->
            spawnPoint = packet.spawnPoint
        }
        ClientPlayNetworking.registerGlobalReceiver(CustomPredicateResponsePayload.TYPE) { packet, _, _ ->
            customPredicateResults[packet.predicateId] = packet.predicateResponse
        }
        ClientPlayNetworking.registerGlobalReceiver(ScoreboardStatePayload.TYPE) { packet, _, _ ->
            scoreboardState[packet.objectiveName] = packet.value
        }
    }

    fun queryCustomPredicate(predicateId: String, predicateString: String): Boolean {
        ClientPlayNetworking.send(CustomPredicateQueryPayload(predicateId, predicateString))

        return customPredicateResults[predicateId] ?: false
    }
}