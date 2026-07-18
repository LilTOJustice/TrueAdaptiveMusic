package liltojustice.trueadaptivemusic.network.processor

import liltojustice.trueadaptivemusic.network.model.CustomPacketPayload
import liltojustice.trueadaptivemusic.network.model.CustomPacketPayloadType

data class ProcessorResult<T: CustomPacketPayload>(
    val type: CustomPacketPayloadType<T>,
    val payload: T
)
