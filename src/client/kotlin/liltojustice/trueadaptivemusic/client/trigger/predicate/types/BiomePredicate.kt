package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.identifier.BiomeIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.util.JsonHelper

class BiomePredicate(private val biomes: List<BiomeIdentifier>): MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean {
        val player = client.player ?: return false
        val playerBiome = client.world?.getBiome(player.blockPos) ?: return false

        return biomes.isEmpty() || biomes.any { biome -> playerBiome.matchesId(biome.identifier) }
    }

    override fun toJson(): JsonObject {
        val result = JsonObject()
        val jsonBiomes = JsonArray()
        biomes.forEach { biome -> jsonBiomes.add(biome.toString()) }
        result.add("id", jsonBiomes)

        return result
    }

    companion object: MusicPredicateCompanion<BiomePredicate> {
        override fun fromJson(json: JsonObject): BiomePredicate {
            return BiomePredicate(
                    if (JsonHelper.hasArray(json, "id"))
                        JsonHelper.getArray(json, "id").map { element -> BiomeIdentifier(element.asString) }
                    else
                        listOf(BiomeIdentifier(JsonHelper.getString(json, "id"))))
        }
    }
}