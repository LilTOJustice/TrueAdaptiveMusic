package liltojustice.trueadaptivemusic.client.trigger.event

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.MusicTriggerException

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
        override fun fromJson(json: JsonObject): MusicEvent {
            throw MusicTriggerException("'Error' event type is invalid and should not be used.")
        }

        const val NAME = "error_predicate"
    }
}