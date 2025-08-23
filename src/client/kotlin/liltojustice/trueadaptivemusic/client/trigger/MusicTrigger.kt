package liltojustice.trueadaptivemusic.client.trigger

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.ReflectionHelper
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.trigger.predicate.TriggerParam

abstract class MusicTrigger {
    var playableSounds: List<PlayableSound> = emptyList()

    fun getTriggerParams(): List<TriggerParam> {
        return ReflectionHelper.getConstructorParameterValues(this)
            .map { param -> TriggerParam(param.name, param.value) }
    }

    fun toJsonFull(): JsonObject {
        val result = JsonObject()
        result.addProperty("type", getTypeName())

        val jsonMusicPath = JsonArray(playableSounds.size)
        playableSounds.forEach { sound -> jsonMusicPath.add(sound.getSoundName()) }
        result.add("musicPath", jsonMusicPath)

        toJson().asMap().forEach { entry -> result.add(entry.key, entry.value) }

        return result
    }

    fun getTriggerId(): String {
        val params = getTriggerParams()
        return getTypeName()  + if (params.isEmpty()) "" else "{${params.joinToString(",")}}"
    }

    abstract fun getTypeName(): String

    protected open fun toJson(): JsonObject {
        return JsonObject()
    }

    companion object: MusicTriggerCompanion<MusicTrigger> {
        fun getTruncatedTriggerId(triggerId: String): String {
            val arrays = Regex("\\[[^]]*]").findAll(triggerId).map { result -> result.value }
            val text = arrays.fold(triggerId) { partial: String, array ->
                partial.replace(array, Regex(",.*").replace(array, ", ...]"))
            }

            return text
        }
    }

    interface MusicTriggerCompanion<TSelf> where TSelf: MusicTrigger {
        fun fromJson(json: JsonObject): TSelf {
            throw MusicTriggerException(
                "Type \"${this::class.qualifiedName}\" must define a fromJson function.")
        }
    }
}
