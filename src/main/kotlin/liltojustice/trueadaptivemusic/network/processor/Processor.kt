package liltojustice.trueadaptivemusic.network.processor

import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

interface Processor {
    fun process(server: MinecraftServer, player: ServerPlayer): CustomPacketPayload?
}