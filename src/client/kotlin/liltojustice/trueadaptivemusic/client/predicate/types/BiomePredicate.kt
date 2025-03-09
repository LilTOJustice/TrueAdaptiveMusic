package liltojustice.trueadaptivemusic.client.predicate.types

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import liltojustice.trueadaptivemusic.client.identifier.BiomeIdentifier
import net.minecraft.client.MinecraftClient
import net.minecraft.util.JsonHelper

class BiomePredicate(private val biome: BiomeIdentifier): MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean {
        return if (client.player != null) client.world?.getBiome(client.player!!.blockPos)?.matchesId(biome) ?: false else false
    }

    override fun toJson(): JsonObject {
        val result = super.toJson()
        result.add("id", JsonPrimitive(biome.toString()))

        return result
    }

    companion object: MusicPredicateCompanion<BiomePredicate> {
        override fun getTypeName(): String { return "biome" }

        override fun fromJson(json: JsonObject): BiomePredicate {
            return BiomePredicate(BiomeIdentifier(JsonHelper.getString(json, "id")))
        }
    }
}