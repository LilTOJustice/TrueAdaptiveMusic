package liltojustice.trueadaptivemusic.client.event.types

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.MusicPack
import liltojustice.trueadaptivemusic.client.MusicTrigger
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicateException
import liltojustice.trueadaptivemusic.client.sound.PlayableSound
import liltojustice.trueadaptivemusic.client.sound.PlayableSoundFile
import kotlin.reflect.KClass

sealed class MusicEvent: MusicTrigger {
    var playableSounds: List<PlayableSound> = emptyList()

    open fun validate(vararg eventArgs: Any?): Boolean {
        return true
    }

    override fun toJson(): JsonObject {
        val result = super.toJson()
        val musicPathJson = JsonArray()
        playableSounds.forEach { playableSound -> musicPathJson.add(playableSound.getSoundName()) }
        result.add("musicPath", musicPathJson)

        return result
    }

    companion object: MusicEventCompanion<MusicEvent> {
        override fun getTypeName(): String {
            throw MusicPredicateException("Attempt to get type name from abstract event type.")
        }
    }

    interface MusicEventCompanion<TSelf>: MusicTrigger.MusicTriggerCompanion<MusicEvent> where TSelf: MusicEvent {
        override fun getImplementingClass(): KClass<MusicEvent> {
            return MusicEvent::class
        }

        fun fromJsonWithLibrary(json: JsonObject, soundLibrary: Map<String, PlayableSoundFile>): MusicEvent {
            val event = super.fromJson(json)
            event.playableSounds = MusicPack.parseMusicPath(json, soundLibrary)
            return event
        }

        fun arrayFromJsonArray(array: JsonArray, soundLibrary: Map<String, PlayableSoundFile>): List<MusicEvent> {
            return array.map { json -> fromJsonWithLibrary(json.asJsonObject, soundLibrary) }
        }
    }
}