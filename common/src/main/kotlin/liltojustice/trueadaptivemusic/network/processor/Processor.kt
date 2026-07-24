package liltojustice.trueadaptivemusic.network.processor

import liltojustice.trueadaptivemusic.network.model.CustomPacketPayload
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

abstract class Processor<T: CustomPacketPayload> {
    protected val tickRate = 20
    private var tick = tickRate

    fun process(server: MinecraftServer, player: ServerPlayer): ProcessorResult<T>? {
        if (tick++ >= tickRate) {
            tick = 0

            return makePacket(server, player)
        }

        return null
    }

    protected abstract fun makePacket(server: MinecraftServer, player: ServerPlayer): ProcessorResult<T>?
}