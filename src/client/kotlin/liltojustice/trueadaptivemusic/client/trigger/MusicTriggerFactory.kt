package liltojustice.trueadaptivemusic.client.trigger

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.music.MusicPack
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSoundFile
import liltojustice.trueadaptivemusic.client.trigger.predicate.ErrorPredicate
import net.minecraft.util.JsonHelper
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions
import kotlin.reflect.full.primaryConstructor

@Suppress("UNCHECKED_CAST")
abstract class MusicTriggerFactory<T: MusicTrigger>(
    private val registry: MusicTriggerRegistry<T>, private val errorFallback: (JsonObject, Exception) -> T) {
    fun fromJson(json: JsonObject, soundLibrary: Map<String, PlayableSoundFile>): T {
        return try {
            val typeName = JsonHelper.getString(json, "type")
            val type = registry[typeName]
            val result = (type.companionObject?.functions?.firstOrNull{ f -> f.name == "fromJson" }
                ?: throw MusicTriggerException("fromJson method missing."))
                .call(type.companionObjectInstance, json) as? MusicTrigger
                ?: throw MusicTriggerException("Could not instantiate music trigger from json.")
            result.playableSounds = MusicPack.parseMusicPath(json, soundLibrary)
            result as? T ?: throw MusicTriggerException("Could not instantiate music trigger from json.")
        } catch (e: MusicTriggerException) {
            errorFallback(json, e)
        }
    }

    fun fromArgs(typeName: String, playableSounds: List<PlayableSound>, vararg args: Any): T {
        val result = getConstructorFromTypeName(typeName).call(*args) as? T
            ?: throw MusicTriggerException("Could not instantiate music trigger from args.")
        result.playableSounds = playableSounds

        return result
    }

    fun getRequiredArgs(typeName: String): List<KParameter> {
        return if (typeName == ErrorPredicate.NAME) emptyList() else getConstructorFromTypeName(typeName).parameters
    }

    private fun getConstructorFromTypeName(typeName: String): KFunction<Any> {
        return registry[typeName].primaryConstructor
            ?: throw MusicTriggerException(
                "Trigger type with name \"$typeName\" has no primary constructor.")
    }
}