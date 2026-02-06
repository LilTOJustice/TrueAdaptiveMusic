package liltojustice.trueadaptivemusic.client.trigger.event.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent

class OnPauseEvent(): MusicEvent() {
    companion object: MusicEventCompanion<OnPauseEvent> {
        override fun fromJson(json: JsonObject): MusicEvent {
            return OnPauseEvent()
        }
    }
}