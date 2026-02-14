package liltojustice.trueadaptivemusic.client.trigger.predicate

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSoundFile
import liltojustice.trueadaptivemusic.client.trigger.MusicTriggerFactory

class MusicPredicateFactory(musicPredicateRegistry: MusicPredicateRegistry)
    : MusicTriggerFactory<MusicPredicate, MusicPredicate.Parameters>(
    musicPredicateRegistry, { json, e -> ErrorPredicate(json, e.message ?: "Unknown") }) {
        override fun fromJson(json: JsonObject, soundLibrary: Map<String, PlayableSoundFile>): MusicPredicate {
            val result = super.fromJson(json, soundLibrary)
            result.ambience = MusicPack.parseAudio("ambiencePath", json, soundLibrary)

            return result
        }

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