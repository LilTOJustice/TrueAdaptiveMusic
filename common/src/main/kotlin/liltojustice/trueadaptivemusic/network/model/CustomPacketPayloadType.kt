package liltojustice.trueadaptivemusic.network.model

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import kotlin.reflect.KClass

interface CustomPacketPayloadType <T: CustomPacketPayload> {
    val identifier: ResourceLocation
    val payloadClass: KClass<T>

    fun read(buf: FriendlyByteBuf): T
    fun write(payload: T, buf: FriendlyByteBuf)
}