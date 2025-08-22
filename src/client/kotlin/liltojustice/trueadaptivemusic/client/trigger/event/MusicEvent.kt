package liltojustice.trueadaptivemusic.client.trigger.event

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.InvokeMusicEventCallback
import liltojustice.trueadaptivemusic.client.music.MusicPack
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSoundFile
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import liltojustice.trueadaptivemusic.client.trigger.MusicTriggerRegistry
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicTriggerException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

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

    override fun getTypeName(): String {
        return if (this is ErrorEvent)
            ErrorEvent.NAME
        else
            registry.getTypeName(this::class)
    }

    companion object: MusicEventCompanion<MusicEvent> {
        private val registry = MusicTriggerRegistry<MusicEvent>()

        fun register(name: String, type: Class<MusicEvent>) {
            registry.register(name, type.kotlin)
        }

        fun register(name: String, type: KClass<MusicEvent>) {
            registry.register(name, type)
        }

        fun getTypeNames(): List<String> {
            return registry.getAll().map { entry -> entry.key }
        }

        fun getNameFromType(type: KClass<out MusicEvent>): String {
            return registry.getTypeName(type)
        }

        fun getNameFromType(type: Class<out MusicEvent>): String {
            return getNameFromType(type.kotlin)
        }

        override fun fromJson(json: JsonObject): MusicEvent {
            return try {
                MusicTrigger
                    .fromJsonProvideSubclasses(json, registry.getAll().map { entry -> entry.value })
            } catch (e: MusicTriggerException) {
                ErrorEvent(json, e.message ?: "Unknown")
            } as MusicEvent
        }
    }

    interface MusicEventCompanion<TSelf>: MusicTrigger.MusicTriggerCompanion<MusicEvent> where TSelf: MusicEvent {
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

        fun getRequiredArgsFromTypeName(typeName: String): List<KParameter> {
            return getConstructorFromTypeName(typeName).parameters
        }

        fun getConstructorFromTypeName(typeName: String): KFunction<Any> {
            return registry.getType(typeName)::class.primaryConstructor
                ?: throw MusicTriggerException(
                    "Trigger type with name \"$typeName\" has no primary constructor.")
        }

        override fun initializeFromArgs(typeName: String, vararg args: Any): MusicEvent {
            return getConstructorFromTypeName(typeName).call(*args) as MusicEvent
        }
    }
}