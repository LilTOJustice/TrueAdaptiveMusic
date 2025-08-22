package liltojustice.trueadaptivemusic.client.trigger.event.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent

class OnRecipeUnlockEvent(): MusicEvent() {
    companion object: MusicEventCompanion<OnRecipeUnlockEvent> {
        override fun fromJson(json: JsonObject): MusicEvent {
            return OnRecipeUnlockEvent()
        }
    }
}