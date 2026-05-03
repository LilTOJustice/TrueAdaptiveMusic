package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.Constants.Companion.NULL_IDENTIFIER
import liltojustice.trueadaptivemusic.network.model.CurrentStructurePayload
import liltojustice.trueadaptivemusic.network.model.CustomPredicateQueryPayload
import liltojustice.trueadaptivemusic.network.model.CustomPredicateResponsePayload
import liltojustice.trueadaptivemusic.network.model.SpawnPoint
import liltojustice.trueadaptivemusic.network.model.SpawnPointPayload
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.util.Identifier

object TAMNetworkingClient {
    var structureId: Identifier = NULL_IDENTIFIER
    var structureSetId: Identifier = NULL_IDENTIFIER
    var spawnPoint: SpawnPoint? = null
    val customPredicateResults = mutableMapOf<String, Boolean>()

    fun init() {
        ClientPlayNetworking.registerGlobalReceiver(CurrentStructurePayload.ID) { payload, _ ->
            structureId = payload.structureIdentifier
            structureSetId = payload.structureSetIdentifier
        }
        ClientPlayNetworking.registerGlobalReceiver(SpawnPointPayload.ID) { payload, _ ->
            spawnPoint = payload.spawnPoint
        }
        ClientPlayNetworking.registerGlobalReceiver(CustomPredicateResponsePayload.ID) { payload, _ ->
            customPredicateResults[payload.predicateId] = payload.predicateResponse
        }
    }

    fun queryCustomPredicate(predicateId: String, predicateString: String): Boolean {
        ClientPlayNetworking.send(CustomPredicateQueryPayload(predicateId, predicateString))

        return customPredicateResults[predicateId] ?: false
    }
}