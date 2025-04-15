package liltojustice.trueadaptivemusic.client.event.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.event.MusicEvent

class OnTutorialPopupEvent(): MusicEvent() {
    companion object: MusicEventCompanion<OnTutorialPopupEvent> {
        override fun getTypeName(): String {
            return "on_tutorial_popup"
        }

        override fun fromJson(json: JsonObject): MusicEvent {
            return OnTutorialPopupEvent()
        }
    }
}