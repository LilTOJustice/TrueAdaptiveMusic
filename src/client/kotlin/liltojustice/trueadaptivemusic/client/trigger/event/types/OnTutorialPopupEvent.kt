package liltojustice.trueadaptivemusic.client.trigger.event.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent

class OnTutorialPopupEvent(): MusicEvent() {
    companion object: MusicEventCompanion<OnTutorialPopupEvent> {
        override fun fromJson(json: JsonObject): MusicEvent {
            return OnTutorialPopupEvent()
        }
    }
}