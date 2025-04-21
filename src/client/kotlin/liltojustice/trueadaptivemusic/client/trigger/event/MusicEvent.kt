package liltojustice.trueadaptivemusic.client.trigger.event

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.InvokeMusicEventCallback
import liltojustice.trueadaptivemusic.ReflectionHelper
import liltojustice.trueadaptivemusic.client.music.MusicPack
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSoundFile
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicTriggerException
import kotlin.reflect.KClass

abstract class MusicEvent: MusicTrigger {
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
            throw MusicTriggerException("Attempt to get type name from abstract event type.")
        }

        override fun fromJson(json: JsonObject): MusicEvent {
            return MusicTrigger.fromJsonProvideSubclasses(json, getTriggerImplementerSubclasses()) as MusicEvent
        }
    }

    interface MusicEventCompanion<TSelf>: MusicTrigger.MusicTriggerCompanion<MusicEvent> where TSelf: MusicEvent {
        override fun getTriggerImplementerSubclasses(): List<KClass<out MusicEvent>> {
            return ReflectionHelper.getSubclassesOf(MusicEvent::class)
        }

        fun fromJsonWithLibrary(json: JsonObject, soundLibrary: Map<String, PlayableSoundFile>): MusicEvent {
            try {
                val event = Companion.fromJson(json)
                event.playableSounds = MusicPack.parseMusicPath(json, soundLibrary)
                return event
            }
            catch (e: Exception) {
                return ErrorEvent(json, e.message ?: "Unknown")
            }
        }

        fun arrayFromJsonArray(array: JsonArray, soundLibrary: Map<String, PlayableSoundFile>): List<MusicEvent> {
            return array.mapNotNull { json -> fromJsonWithLibrary(json.asJsonObject, soundLibrary) }
        }

        fun invokeMusicEvent(eventName: String, vararg eventArgs: Any?) {
            InvokeMusicEventCallback.EVENT.invoker().invokeMusicEvent(eventName, *eventArgs)
        }
    }
}