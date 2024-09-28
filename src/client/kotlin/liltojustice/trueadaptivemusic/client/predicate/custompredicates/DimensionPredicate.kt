package liltojustice.trueadaptivemusic.client.predicate.custompredicates

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper

class DimensionPredicate internal constructor(private val dimension: Identifier) : MusicPredicate {
    override fun test(client: MinecraftClient): Boolean {
        // TODO: Implement this
        return false
    }

    companion object: MusicPredicate.MusicPredicateCompanion<DimensionPredicate> {
        override fun getTypeName(): String { return "dimension" }

        override fun fromJson(json: JsonObject): DimensionPredicate {
            val type = JsonHelper.getString(json, "type")
            if (type != getTypeName())
            {
                throw Exception("Unexpected type. $type")
            }

            val id: String = JsonHelper.getString(json, "id")

            return DimensionPredicate(Identifier(id))
        }
    }
}