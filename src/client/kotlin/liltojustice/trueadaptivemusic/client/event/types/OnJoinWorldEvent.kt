package liltojustice.trueadaptivemusic.client.event.types

import com.google.gson.JsonObject

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