package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.Constants.Companion.NULL_IDENTIFIER
import liltojustice.trueadaptivemusic.network.model.CurrentStructurePayload
import liltojustice.trueadaptivemusic.network.model.CustomPredicateQueryPayload
import liltojustice.trueadaptivemusic.network.model.CustomPredicateResponsePayload
import liltojustice.trueadaptivemusic.network.model.ScoreboardStatePayload
import liltojustice.trueadaptivemusic.network.model.SpawnPointPayload
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.resources.Identifier
import net.minecraft.world.level.storage.LevelData

object TAMNetworkingClient {
    var structureId: Identifier = NULL_IDENTIFIER
    var structureSetId: Identifier = NULL_IDENTIFIER
    var structurePieceId: Identifier = NULL_IDENTIFIER
    var spawnPoint: LevelData.RespawnData? = null
    val customPredicateResults = mutableMapOf<String, Boolean>()
    val scoreboardState = mutableMapOf<String, Int>()

    fun init() {
        ClientPlayNetworking.registerGlobalReceiver(CurrentStructurePayload.TYPE) { payload, _ ->
            structureId = payload.structureIdentifier
            structureSetId = payload.structureSetIdentifier
            structurePieceId = payload.structurePieceIdentifier
        }
        ClientPlayNetworking.registerGlobalReceiver(SpawnPointPayload.TYPE) { payload, _ ->
            spawnPoint = payload.spawnPoint
        }
        ClientPlayNetworking.registerGlobalReceiver(CustomPredicateResponsePayload.TYPE) { payload, _ ->
            customPredicateResults[payload.predicateId] = payload.predicateResponse
        }
        ClientPlayNetworking.registerGlobalReceiver(ScoreboardStatePayload.TYPE) { payload, _ ->
            scoreboardState[payload.objectiveName] = payload.value
        }
    }

    fun queryCustomPredicate(predicateId: String, predicateString: String): Boolean {
        ClientPlayNetworking.send(CustomPredicateQueryPayload(predicateId, predicateString))

        return customPredicateResults[predicateId] ?: false
    }
}