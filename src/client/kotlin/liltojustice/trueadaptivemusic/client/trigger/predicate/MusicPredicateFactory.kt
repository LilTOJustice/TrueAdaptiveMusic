package liltojustice.trueadaptivemusic.client.trigger.predicate

import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.trigger.MusicTriggerFactory

class MusicPredicateFactory(musicPredicateRegistry: MusicPredicateRegistry)
    : MusicTriggerFactory<MusicPredicate, MusicPredicate.Parameters>(
    musicPredicateRegistry, { json, e -> ErrorPredicate(json, e.message ?: "Unknown") }) {
        fun fromArgs(
            typeName: String,
            music: List<PlayableSound>,
            ambience: List<PlayableSound>,
            parameters: List<Any>,
            args: List<Any>): MusicPredicate {
            val result = super.fromArgs(typeName, music, parameters, args)
            result.ambience = ambience

            return result
    }
}