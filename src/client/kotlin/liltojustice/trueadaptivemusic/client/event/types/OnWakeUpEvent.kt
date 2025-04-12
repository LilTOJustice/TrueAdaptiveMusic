package liltojustice.trueadaptivemusic.client.event.types

import com.google.gson.JsonObject

class OnWakeUpEvent(): MusicEvent() {
    companion object: MusicEventCompanion<OnWakeUpEvent> {
        override fun getTypeName(): String {
            return "on_wake_up"
        }

        override fun fromJson(json: JsonObject): MusicEvent {
            return OnWakeUpEvent()
        }
    }
}