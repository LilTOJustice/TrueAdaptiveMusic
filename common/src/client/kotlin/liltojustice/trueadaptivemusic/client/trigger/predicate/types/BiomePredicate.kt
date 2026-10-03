package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.cache.TAMClientCache
import liltojustice.trueadaptivemusicapi.identifier.BiomeIdentifier
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.PredicateType
import liltojustice.trueadaptivemusicapi.trigger.state.TriggerState
import net.minecraft.client.Minecraft
import kotlin.reflect.typeOf

object BiomePredicate: PredicateType<BiomePredicate.Arguments, BiomePredicate.State>(
    "biome", typeOf<Arguments>()
) {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::biomes.name to "Select all biomes the music should play for. If none, any biome will trigger " +
                    "the music."
        )

    override fun test(arguments: Arguments, state: State): Boolean {
        val minecraft = Minecraft.getInstance()
        val player = minecraft.player ?: return false
        val playerBiome = if (state.hasRiverBiome) {
            player.level().getBiome(player.blockPosition())
        }
        else {
            TAMClientCache.lastNonRiverBiome ?: return false
        }

        return arguments.biomes.isEmpty() || arguments.biomes.any { it.matches(playerBiome) }
    }

    override fun createState(arguments: Arguments): State {
        return State(arguments.biomes.any { isRiverBiome(it) })
    }

    private fun isRiverBiome(identifier: BiomeIdentifier): Boolean {
        return identifier.path.contains("river")
    }

    data class Arguments(val biomes: List<BiomeIdentifier>): TriggerArguments()

    data class State(val hasRiverBiome: Boolean): TriggerState()
}