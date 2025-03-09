package liltojustice.trueadaptivemusic.client.predicate.types

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import liltojustice.trueadaptivemusic.client.identifier.DimensionIdentifier
import net.minecraft.client.MinecraftClient
import net.minecraft.util.JsonHelper

class DimensionPredicate(private val dimension: DimensionIdentifier): MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean {
        return client.player?.world?.dimensionEntry?.matchesId(dimension) ?: false
    }

    override fun toJson(): JsonObject {
        val result = super.toJson()
        result.add("id", JsonPrimitive(dimension.toString()))

        return result
    }

    companion object: MusicPredicateCompanion<DimensionPredicate> {
        override fun getTypeName(): String { return "dimension" }

        override fun fromJson(json: JsonObject): DimensionPredicate {
            return DimensionPredicate(DimensionIdentifier(JsonHelper.getString(json, "id")))
        }
    }
}