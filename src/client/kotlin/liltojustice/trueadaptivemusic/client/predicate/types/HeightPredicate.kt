package liltojustice.trueadaptivemusic.client.predicate.types

import com.google.gson.JsonObject
import net.minecraft.client.MinecraftClient
import net.minecraft.util.JsonHelper

class HeightPredicate(private val above: Boolean, private val y: Int): MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean {
        val playerHeight = client.player?.blockPos?.y ?: return false

        return if (above) playerHeight >= y else playerHeight <= y
    }

    override fun toJson(): JsonObject {
        val result = super.toJson()
        result.addProperty("above", above)
        result.addProperty("y", y)

        return result
    }

    companion object: MusicPredicateCompanion<HeightPredicate> {
        override fun getTypeName(): String { return "height" }

        override fun fromJson(json: JsonObject): HeightPredicate {
            return HeightPredicate(JsonHelper.getBoolean(json, "above"), JsonHelper.getInt(json, "y"))
        }
    }
}