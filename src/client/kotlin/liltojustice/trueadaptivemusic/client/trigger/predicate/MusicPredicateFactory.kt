package liltojustice.trueadaptivemusic.client.trigger.predicate

import liltojustice.trueadaptivemusic.client.trigger.MusicTriggerFactory

class MusicPredicateFactory(musicPredicateRegistry: MusicPredicateRegistry): MusicTriggerFactory<MusicPredicate>(
    musicPredicateRegistry, { json, e -> ErrorPredicate(json, e.message ?: "Unknown") }) {
}