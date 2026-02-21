package liltojustice.trueadaptivemusic.client.trigger.event

import com.google.gson.JsonObject

class ErrorEvent(private val actualJson: JsonObject, val reason: String): MusicEvent() {
    val shortenedJson: JsonObject = run {
        val shortened = actualJson.deepCopy()
        shortened.remove("musicPath")

        shortened
    }

    override fun toJson(): JsonObject {
        return actualJson
    }

    companion object: MusicEventCompanion<ErrorEvent> {
        const val NAME = "error_predicate"
    }
}