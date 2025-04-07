package liltojustice.trueadaptivemusic.client.event.types

import liltojustice.trueadaptivemusic.client.MusicTrigger
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicateException
import kotlin.reflect.KClass

abstract class MusicEvent: MusicTrigger {
    companion object: MusicEventCompanion<MusicEvent> {
        override fun getTypeName(): String {
            throw MusicPredicateException("Attempt to get type name from abstract event type.")
        }
    }

    interface MusicEventCompanion<TSelf>: MusicTrigger.MusicTriggerCompanion<MusicEvent> where TSelf: MusicEvent {
        override fun getImplementingClass(): KClass<MusicEvent> {
            return MusicEvent::class
        }
    }
}