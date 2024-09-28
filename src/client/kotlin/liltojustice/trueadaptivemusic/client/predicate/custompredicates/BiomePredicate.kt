package liltojustice.trueadaptivemusic.client.predicate.custompredicates

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicate
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicateException
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper

class BiomePredicate internal constructor(partialPath: String, private val biome: Identifier) : MusicPredicate(partialPath) {
    override fun test(client: MinecraftClient): Boolean {
        // TODO: Implement this
        return false
    }

    override fun getIDs(): List<String> { return listOf(biome.toString()) }

    companion object: MusicPredicate.MusicPredicateCompanion<BiomePredicate> {
        override fun getTypeName(): String { return "biome" }

        override fun fromJson(json: JsonObject, partialPath: String): BiomePredicate {
            return BiomePredicate(partialPath, Identifier(JsonHelper.getString(json, "id")))
        }
    }
}