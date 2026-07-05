package liltojustice.trueadaptivemusic.common.network.processor

import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

abstract class Processor {
    protected val tickRate = 20
    private var tick = tickRate

    fun process(server: MinecraftServer, player: ServerPlayer): CustomPacketPayload? {
        if (tick++ >= tickRate) {
            tick = 0

            return makePacket(server, player)
        }

        return null
    }

    protected abstract fun makePacket(server: MinecraftServer, player: ServerPlayer): CustomPacketPayload?
}