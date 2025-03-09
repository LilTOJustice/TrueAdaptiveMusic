package liltojustice.trueadaptivemusic.client.predicate.types

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.identifier.DimensionIdentifier
import net.minecraft.client.MinecraftClient
import net.minecraft.util.JsonHelper

class DimensionPredicate(private val dimensions: List<DimensionIdentifier>): MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean {
        val playerDimension = client.player?.world?.dimensionEntry ?: return false

        return dimensions.any { dimension -> playerDimension.matchesId(dimension) }
    }

    override fun toJson(): JsonObject {
        val result = super.toJson()
        val jsonDimensions = JsonArray()
        dimensions.forEach { dimension -> jsonDimensions.add(dimension.toString()) }
        result.add("id", jsonDimensions)

        return result
    }

    companion object: MusicPredicateCompanion<DimensionPredicate> {
        override fun getTypeName(): String { return "dimension" }

        override fun fromJson(json: JsonObject): DimensionPredicate {
            return DimensionPredicate(
                if (JsonHelper.hasArray(json, "id"))
                    JsonHelper.getArray(json, "id").map { element -> DimensionIdentifier(element.asString) }
                else
                    listOf(DimensionIdentifier(JsonHelper.getString(json, "id"))))
        }
    }
}