package liltojustice.trueadaptivemusic.network.model

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation

interface CustomPacketPayloadType <T: CustomPacketPayload> {
    val identifier: ResourceLocation

    fun read(buf: FriendlyByteBuf): T
    fun write(payload: T, buf: FriendlyByteBuf)
}