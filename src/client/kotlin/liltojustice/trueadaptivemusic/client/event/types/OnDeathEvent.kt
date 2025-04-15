package liltojustice.trueadaptivemusic.client.event.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.event.MusicEvent

class OnDeathEvent(): MusicEvent() {
    companion object: MusicEventCompanion<OnDeathEvent> {
        override fun getTypeName(): String {
            return "on_death"
        }

        override fun fromJson(json: JsonObject): MusicEvent {
            return OnDeathEvent()
        }
    }
}