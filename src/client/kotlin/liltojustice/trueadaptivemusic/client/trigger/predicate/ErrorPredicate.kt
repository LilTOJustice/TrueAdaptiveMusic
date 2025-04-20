package liltojustice.trueadaptivemusic.client.trigger.predicate

import com.google.gson.JsonObject
import net.minecraft.client.MinecraftClient

class ErrorPredicate(private val actualJson: JsonObject, val reason: String): MusicPredicate() {
    val shortenedJson: JsonObject = run {
        val shortened = actualJson.deepCopy()
        shortened.remove("musicPath")
        shortened.remove("children")
        shortened.remove("events")
        shortened.remove("parameters")

        shortened
    }

    override fun test(client: MinecraftClient): Boolean {
        return false
    }

    override fun toJson(): JsonObject {
        return actualJson
    }

    companion object: MusicPredicateCompanion<ErrorPredicate> {
        override fun getTypeName(): String {
            return "error_predicate"
        }

        override fun fromJson(json: JsonObject): MusicPredicate {
            throw MusicTriggerException("'Error' predicate type is invalid and should not be used.")
        }
    }
}