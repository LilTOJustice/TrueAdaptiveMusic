package liltojustice.trueadaptivemusic.client.predicate.custompredicates

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper

class BiomePredicate internal constructor(private val biome: Identifier) : MusicPredicate {
    override fun test(client: MinecraftClient): Boolean {
        // TODO: Implement this
        return false
    }

    companion object: MusicPredicate.MusicPredicateCompanion<BiomePredicate> {
        override fun getTypeName(): String { return "biome" }

        override fun fromJson(json: JsonObject): BiomePredicate {
            val type = JsonHelper.getString(json, "type")
            if (type != getTypeName())
            {
                throw Exception("Unexpected type. $type")
            }

            val id: String = JsonHelper.getString(json, "id")

            return BiomePredicate(Identifier(id))
        }
    }
}