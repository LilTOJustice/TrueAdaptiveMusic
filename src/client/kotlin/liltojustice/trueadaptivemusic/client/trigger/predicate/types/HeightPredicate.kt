package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.util.JsonHelper

class HeightPredicate(private val above: Boolean, private val y: Int): MusicPredicate() {
    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        val playerHeight = client.player?.blockPos?.y ?: return false

        return if (above) playerHeight >= y else playerHeight <= y
    }

    override fun toJson(): JsonObject {
        val result = JsonObject()
        result.addProperty("above", above)
        result.addProperty("y", y)

        return result
    }

    companion object: MusicPredicateCompanion<HeightPredicate> {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                "above" to "Whether the music should play when the player is above or below the y value.",
                "y" to "Threshold at which the predicate should switch."
            )

        override fun fromJson(json: JsonObject): HeightPredicate {
            return HeightPredicate(
                JsonHelper.getBoolean(json, "above"),
                JsonHelper.getInt(json, "y")
            )
        }
    }
}