package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.BiomeIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class BiomePredicate(private val biomes: List<BiomeIdentifier>): MusicPredicate() {
    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        val player = client.player ?: return false
        val playerBiome = client.world?.getBiome(player.blockPos) ?: return false

        return biomes.isEmpty() || biomes.any { biome -> playerBiome.matchesId(biome.id) }
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                "biomes" to "Select all biomes the music should play for. If none, any biome will trigger the music."
            )
    }
}