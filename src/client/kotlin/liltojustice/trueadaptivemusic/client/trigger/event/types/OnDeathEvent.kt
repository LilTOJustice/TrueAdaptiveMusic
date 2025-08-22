package liltojustice.trueadaptivemusic.client.trigger.event.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent

class OnDeathEvent(): MusicEvent() {
    companion object: MusicEventCompanion<OnDeathEvent> {
        override fun fromJson(json: JsonObject): MusicEvent {
            return OnDeathEvent()
        }
    }
}