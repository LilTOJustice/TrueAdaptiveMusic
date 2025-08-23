package liltojustice.trueadaptivemusic.client.trigger.event.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent

class OnWakeUpEvent(): MusicEvent() {
    companion object: MusicEventCompanion<OnWakeUpEvent> {
        override fun fromJson(json: JsonObject): MusicEvent {
            return OnWakeUpEvent()
        }
    }
}