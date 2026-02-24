package liltojustice.trueadaptivemusic.client.trigger.predicate

import com.google.gson.JsonObject

class ErrorPredicate(val actualJson: JsonObject, val reason: String): MusicPredicate() {
    val shortenedJson: JsonObject = run {
        val shortened = actualJson.deepCopy()
        shortened.remove("musicPath")
        shortened.remove("children")
        shortened.remove("events")
        shortened.remove("parameters")

        shortened
    }

    override fun test(): Boolean {
        return false
    }

    companion object: MusicPredicateCompanion {
        const val NAME = "error_predicate"
    }
}