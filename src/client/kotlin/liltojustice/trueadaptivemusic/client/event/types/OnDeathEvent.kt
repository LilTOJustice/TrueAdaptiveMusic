package liltojustice.trueadaptivemusic.client.event.types

import com.google.gson.JsonObject

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