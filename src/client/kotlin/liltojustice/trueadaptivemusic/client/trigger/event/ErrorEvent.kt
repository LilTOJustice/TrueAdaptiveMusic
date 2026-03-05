package liltojustice.trueadaptivemusic.client.trigger.event

import com.google.gson.JsonObject

class ErrorEvent(val actualJson: JsonObject, val reason: String): MusicEvent() {
    val shortenedJson: JsonObject = run {
        val shortened = actualJson.deepCopy()
        shortened.remove("musicPath")

        shortened
    }

    companion object: MusicEventCompanion {
        const val NAME = "error_event"
    }
}