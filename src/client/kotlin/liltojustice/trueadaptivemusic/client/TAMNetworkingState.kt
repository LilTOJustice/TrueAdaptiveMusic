package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.Constants.Companion.NULL_IDENTIFIER
import liltojustice.trueadaptivemusic.network.model.CurrentStructurePayload
import liltojustice.trueadaptivemusic.network.model.CurrentStructureSetPayload
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.resources.Identifier

object TAMNetworkingState {
    var structureId: Identifier = NULL_IDENTIFIER
    var structureSetId: Identifier = NULL_IDENTIFIER

    fun init() {
        ClientPlayNetworking.registerGlobalReceiver(CurrentStructurePayload.TYPE) { payload, _ ->
            structureId = payload.structureIdentifier
        }

        ClientPlayNetworking.registerGlobalReceiver(CurrentStructureSetPayload.TYPE) { payload, _ ->
            structureSetId = payload.structureSetIdentifier
        }
    }
}