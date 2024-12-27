package liltojustice.trueadaptivemusic.client.predicate

import com.google.gson.JsonObject
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper

class BiomePredicate(partialPath: String, private val biome: Identifier)
    : MusicPredicate(partialPath) {
    override fun test(client: MinecraftClient): Boolean {
        return if (client.player != null) client.world?.getBiome(client.player!!.blockPos)?.matchesId(biome) ?: false else false
    }

    override fun getIDs(): List<String> { return listOf(biome.toString()) }

    companion object: MusicPredicateCompanion<BiomePredicate> {
        override fun getTypeName(): String { return "biome" }

        override fun fromJson(json: JsonObject, partialPath: String): BiomePredicate {
            return BiomePredicate(partialPath, Identifier(JsonHelper.getString(json, "id")))
        }
    }
}