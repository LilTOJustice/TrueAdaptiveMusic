package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.identifier.DimensionIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.util.JsonHelper

class DimensionPredicate(private val dimensions: List<DimensionIdentifier>): MusicPredicate() {
    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        val playerDimension = client.player?.entityWorld?.dimensionEntry ?: return false

        return dimensions.isEmpty() ||
                dimensions.any { dimension -> playerDimension.matchesId(dimension.identifier) }
    }

    override fun toJson(): JsonObject {
        val result = JsonObject()
        val jsonDimensions = JsonArray()
        dimensions.forEach { dimension -> jsonDimensions.add(dimension.toString()) }
        result.add("id", jsonDimensions)

        return result
    }

    companion object: MusicPredicateCompanion<DimensionPredicate> {
        override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            "dimensions" to "Select all dimensions the music should play for. If none, any dimension will trigger " +
                    "the music."
        )

        override fun fromJson(json: JsonObject): DimensionPredicate {
            return DimensionPredicate(
                if (JsonHelper.hasArray(json, "id"))
                    JsonHelper.getArray(json, "id").map { element -> DimensionIdentifier(element.asString) }
                else
                    listOf(DimensionIdentifier(JsonHelper.getString(json, "id")))
            )
        }
    }
}