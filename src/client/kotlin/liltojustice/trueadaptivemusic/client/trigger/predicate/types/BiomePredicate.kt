package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.BiomeIdentifier
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft
import kotlin.reflect.typeOf

object BiomePredicate: StaticPredicateType<BiomePredicate.Arguments>(
    "biome", typeOf<Arguments>()
) {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::biomes.name to "Select all biomes the music should play for. If none, any biome will trigger " +
                    "the music."
        )

    override fun test(arguments: Arguments): Boolean {
        val minecraft = Minecraft.getInstance()
        val player = minecraft.player ?: return false
        val playerBiome = minecraft.level?.getBiome(player.blockPosition()) ?: return false

        return arguments.biomes.isEmpty() || arguments.biomes.any { biome -> biome.matches(playerBiome) }
    }

    data class Arguments(val biomes: List<BiomeIdentifier>): TriggerArguments()
}