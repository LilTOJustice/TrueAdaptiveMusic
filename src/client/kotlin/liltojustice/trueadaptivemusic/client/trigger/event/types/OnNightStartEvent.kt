package liltojustice.trueadaptivemusic.client.trigger.event.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent

class OnNightStartEvent(): MusicEvent() {
    companion object: MusicEventCompanion<OnNightStartEvent> {
        override fun fromJson(json: JsonObject): MusicEvent {
            return OnNightStartEvent()
        }
    }
}