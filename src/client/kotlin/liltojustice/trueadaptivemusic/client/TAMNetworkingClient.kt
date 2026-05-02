package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.Constants.Companion.NULL_IDENTIFIER
import liltojustice.trueadaptivemusic.network.model.CurrentStructurePayload
import liltojustice.trueadaptivemusic.network.model.SpawnPointPayload
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.resources.Identifier
import net.minecraft.world.level.storage.LevelData

object TAMNetworkingClient {
    var structureId: Identifier = NULL_IDENTIFIER
    var structureSetId: Identifier = NULL_IDENTIFIER
    var spawnPoint: LevelData.RespawnData? = null

    fun init() {
        ClientPlayNetworking.registerGlobalReceiver(CurrentStructurePayload.TYPE) { payload, _ ->
            structureId = payload.structureIdentifier
            structureSetId = payload.structureSetIdentifier
        }
        ClientPlayNetworking.registerGlobalReceiver(SpawnPointPayload.TYPE) { payload, _ ->
            spawnPoint = payload.spawnPoint
        }
    }
}