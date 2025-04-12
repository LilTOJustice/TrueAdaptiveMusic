package liltojustice.trueadaptivemusic.client.event.types

import com.google.gson.JsonObject

class OnNightStartEvent(): MusicEvent() {
    companion object: MusicEventCompanion<OnNightStartEvent> {
        override fun getTypeName(): String {
            return "on_night_start"
        }

        override fun fromJson(json: JsonObject): MusicEvent {
            return OnNightStartEvent()
        }
    }
}