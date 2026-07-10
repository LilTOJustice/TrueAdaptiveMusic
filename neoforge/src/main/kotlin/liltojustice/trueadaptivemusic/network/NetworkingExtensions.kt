package liltojustice.trueadaptivemusic.network

import liltojustice.trueadaptivemusic.network.model.Context
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.neoforged.neoforge.network.handling.IPayloadContext
import net.neoforged.neoforge.network.handling.IPayloadHandler

fun <T: CustomPacketPayload> transformHandler(
    handler: (payload: T, context: Context) -> Unit): IPayloadHandler<T> {
    return { payload, context -> handler(payload, context.toCommonContext()) }
}

fun IPayloadContext.toCommonContext(): Context {
    return Context(this.player())
}
