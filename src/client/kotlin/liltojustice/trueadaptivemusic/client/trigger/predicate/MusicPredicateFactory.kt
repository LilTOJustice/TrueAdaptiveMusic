package liltojustice.trueadaptivemusic.client.trigger.predicate

import liltojustice.trueadaptivemusic.client.trigger.MusicTriggerFactory

object MusicPredicateFactory: MusicTriggerFactory<MusicPredicate>(
    MusicPredicateRegistry, { json, e -> ErrorPredicate(json, e.message ?: "Unknown") }) {
}