package liltojustice.trueadaptivemusic.client.trigger.event.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent

class OnJoinWorldEvent(): MusicEvent() {
    companion object: MusicEventCompanion<OnJoinWorldEvent> {
        override fun getTypeName(): String {
            return "on_join_world"
        }

        override fun fromJson(json: JsonObject): MusicEvent {
            return OnJoinWorldEvent()
        }
    }
}