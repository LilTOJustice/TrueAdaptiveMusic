package liltojustice.trueadaptivemusic.client.event.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.event.MusicEvent

class OnAdvancementGetEvent(): MusicEvent() {
    companion object: MusicEventCompanion<OnAdvancementGetEvent> {
        override fun getTypeName(): String {
            return "on_advancement_get"
        }

        override fun fromJson(json: JsonObject): MusicEvent {
            return OnAdvancementGetEvent()
        }
    }
}