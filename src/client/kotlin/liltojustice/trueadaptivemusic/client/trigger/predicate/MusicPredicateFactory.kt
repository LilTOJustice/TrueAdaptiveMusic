package liltojustice.trueadaptivemusic.client.trigger.predicate

import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.PredicateTypeBase

class MusicPredicateFactory {
    fun fromArgs(predicateType: PredicateTypeBase, arguments: TriggerArguments): MusicPredicate<*> {
        return MusicPredicate(predicateType, arguments, predicateType.createStateBase(arguments))
    }
}