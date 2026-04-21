package liltojustice.trueadaptivemusic.client.trigger.predicate

import liltojustice.trueadaptivemusicapi.trigger.predicate.arguments.PredicateArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.state.PredicateState
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.PredicateType

class MusicPredicateFactory {
    fun fromArgs(predicateType: PredicateType<*, *>, arguments: PredicateArguments): MusicPredicate<*, *, *> {
        val predicateType = predicateType as PredicateType<PredicateArguments, PredicateState>
        return MusicPredicate(predicateType, arguments, predicateType.create(arguments))
    }

    fun makeCopy()
}