package liltojustice.trueadaptivemusic.network.processor

import net.minecraft.network.packet.CustomPayload
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity

abstract class Processor {
    protected val tickRate = 20
    private var tick = tickRate

    fun process(server: MinecraftServer, player: ServerPlayerEntity): CustomPayload? {
        if (tick++ >= tickRate) {
            tick = 0

            return makePacket(server, player)
        }

        return null
    }

    protected abstract fun makePacket(server: MinecraftServer, player: ServerPlayerEntity): CustomPayload?
}