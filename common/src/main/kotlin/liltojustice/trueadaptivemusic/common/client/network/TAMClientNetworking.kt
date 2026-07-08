package liltojustice.trueadaptivemusic.common.client.network

import liltojustice.trueadaptivemusic.common.Constants
import liltojustice.trueadaptivemusic.common.network.model.CurrentStructurePayload
import liltojustice.trueadaptivemusic.common.network.model.CustomPredicateQueryPayload
import liltojustice.trueadaptivemusic.common.network.model.CustomPredicateResponsePayload
import liltojustice.trueadaptivemusic.common.network.model.ScoreboardStatePayload
import liltojustice.trueadaptivemusic.common.network.model.SpawnPointPayload
import liltojustice.trueadaptivemusic.network.NetworkingCommon
import net.minecraft.resources.Identifier
import net.minecraft.world.level.storage.LevelData

object TAMClientNetworking {
    var structureId: Identifier = Constants.NULL_IDENTIFIER
    var structureSetId: Identifier = Constants.NULL_IDENTIFIER
    var structurePieceId: Identifier = Constants.NULL_IDENTIFIER
    var spawnPoint: LevelData.RespawnData? = null
    val customPredicateResults = mutableMapOf<String, Boolean>()
    val scoreboardState = mutableMapOf<String, Int>()

    fun init() {
        NetworkingCommon.registerClientboundPacket(
            CurrentStructurePayload.TYPE, CurrentStructurePayload.CODEC) { payload, _ ->
            structureId = payload.structureIdentifier
            structureSetId = payload.structureSetIdentifier
            structurePieceId = payload.structurePieceIdentifier
        }
        NetworkingCommon.registerClientboundPacket(SpawnPointPayload.TYPE, SpawnPointPayload.CODEC) { payload, _ ->
            spawnPoint = payload.spawnPoint
        }
        NetworkingCommon.registerClientboundPacket(
            CustomPredicateResponsePayload.TYPE, CustomPredicateResponsePayload.CODEC) { payload, _ ->
            customPredicateResults[payload.predicateId] = payload.predicateResponse
        }
        NetworkingCommon.registerClientboundPacket(
            ScoreboardStatePayload.TYPE, ScoreboardStatePayload.CODEC) { payload, _ ->
            scoreboardState[payload.objectiveName] = payload.value
        }
    }

    fun queryCustomPredicate(predicateId: String, predicateString: String): Boolean {
        NetworkingCommon.sendToServer(CustomPredicateQueryPayload(predicateId, predicateString))

        return customPredicateResults[predicateId] ?: false
    }
}