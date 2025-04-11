package liltojustice.trueadaptivemusic.client.event.types

import com.google.gson.JsonObject

class OnDayStartEvent(): MusicEvent() {
    companion object: MusicEventCompanion<OnDayStartEvent> {
        override fun getTypeName(): String {
            return "on_day_start"
        }

        override fun fromJson(json: JsonObject): MusicEvent {
            return OnDayStartEvent()
        }
    }
}