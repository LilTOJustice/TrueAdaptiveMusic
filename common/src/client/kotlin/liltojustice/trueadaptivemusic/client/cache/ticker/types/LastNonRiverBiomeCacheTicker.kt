package liltojustice.trueadaptivemusic.client.cache.ticker.types

import liltojustice.trueadaptivemusic.client.cache.ticker.CacheTicker
import net.minecraft.client.Minecraft
import net.minecraft.core.Holder
import net.minecraft.world.level.biome.Biome
import kotlin.jvm.optionals.getOrNull

class LastNonRiverBiomeCacheTicker: CacheTicker<Holder<Biome>?>() {
    override val tickRate: UInt = 10U

    private var lastNonRiverBiome: Holder<Biome>? = null

    override fun tickBase(minecraft: Minecraft) {
        val player = minecraft.player ?: return
        val biome = player.level().getBiome(player.blockPosition())

        if (biome.unwrapKey().getOrNull()?.identifier()?.path?.contains("river") ?: false) {
            return
        }

        lastNonRiverBiome = biome
    }

    override fun getValue(): Holder<Biome>? {
        return lastNonRiverBiome
    }
}