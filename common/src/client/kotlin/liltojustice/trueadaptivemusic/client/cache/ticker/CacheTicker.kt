package liltojustice.trueadaptivemusic.client.cache.ticker

import net.minecraft.client.Minecraft

abstract class CacheTicker<TCacheValue> {
    abstract val tickRate: UInt

    private var ticksSince = 0U

    fun tick(minecraft: Minecraft) {
        if (ticksSince == 0U || ticksSince >= tickRate) {
            ticksSince = 1U

            tickBase(minecraft)
        }

        ticksSince++
    }

    abstract fun getValue(): TCacheValue

    protected abstract fun tickBase(minecraft: Minecraft)
}