package liltojustice.trueadaptivemusic.client.trigger.event.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent

class OnEnterPredicateEvent: MusicEvent() {
    companion object: MusicEventCompanion<OnEnterPredicateEvent> {
        override fun getTypeName(): String {
            return "on_enter_predicate"
        }

        override fun fromJson(json: JsonObject): MusicEvent {
            return OnEnterPredicateEvent()
        }
    }
}