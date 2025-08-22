package liltojustice.trueadaptivemusic.client.trigger.event.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent

class OnDayStartEvent(): MusicEvent() {
    companion object: MusicEventCompanion<OnDayStartEvent> {
        override fun fromJson(json: JsonObject): MusicEvent {
            return OnDayStartEvent()
        }
    }
}