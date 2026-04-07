package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.BiomeIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft

class BiomePredicate(private val biomes: List<BiomeIdentifier>): MusicPredicate() {
    override fun test(): Boolean {
        val minecraft = Minecraft.getInstance()
        val player = minecraft.player ?: return false
        val playerBiome = minecraft.level?.getBiome(player.blockPosition()) ?: return false

        return biomes.isEmpty() || biomes.any { biome -> playerBiome.`is`(biome.id) }
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                BiomePredicate::biomes.name to "Select all biomes the music should play for. If none, any biome will " +
                        "trigger the music."
            )
    }
}