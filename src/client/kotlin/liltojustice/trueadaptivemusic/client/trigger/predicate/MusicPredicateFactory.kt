package liltojustice.trueadaptivemusic.client.trigger.predicate

import liltojustice.trueadaptivemusic.ReflectionHelper
import liltojustice.trueadaptivemusic.client.trigger.MusicTriggerFactory

class MusicPredicateFactory(musicPredicateRegistry: MusicPredicateRegistry)
    : MusicTriggerFactory<MusicPredicate>(musicPredicateRegistry) {
    fun makeCopy(musicPredicate: MusicPredicate): MusicPredicate {
        return fromArgs(
            musicPredicate.getTypeName(),
            ReflectionHelper.getConstructorParameterValues(musicPredicate)
                .mapNotNull { it.value }
        )
    }
}