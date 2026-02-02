package liltojustice.trueadaptivemusic.client.trigger

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.ReflectionHelper
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.trigger.predicate.TriggerArg
import liltojustice.trueadaptivemusic.client.trigger.predicate.TriggerParam
import kotlin.reflect.full.primaryConstructor

abstract class MusicTrigger<TParameters: MusicTrigger.Parameters> {
    var playableSounds: List<PlayableSound> = emptyList()
    lateinit var parameters: TParameters

    fun getTriggerArgs(): List<TriggerArg> {
        return ReflectionHelper.getConstructorParameterValues(this)
            .map { arg -> TriggerArg(arg.name, arg.value) }
    }

    fun toJsonFull(): JsonObject {
        val result = JsonObject()
        result.addProperty("type", getTypeName())

        val jsonMusicPath = JsonArray(playableSounds.size)
        playableSounds.forEach { sound -> jsonMusicPath.add(sound.getSoundName()) }
        result.add("musicPath", jsonMusicPath)
        result.add("parameters", paramsJson())

        toJson().asMap().forEach { entry -> result.add(entry.key, entry.value) }

        return result
    }

    fun getTriggerId(): String {
        val args = getTriggerArgs()
        return getTypeName()  + if (args.isEmpty()) "" else "{${args.joinToString(",")}}"
    }

    private fun paramsJson(): JsonObject {
        return Gson().toJsonTree(parameters).asJsonObject
    }

    abstract fun getTypeName(): String

    abstract fun initParams(json: JsonObject)

    protected open fun toJson(): JsonObject {
        return JsonObject()
    }

    companion object: MusicTriggerCompanion<MusicTrigger<*>> {
        fun getTruncatedTriggerId(triggerId: String): String {
            val arrays = Regex("\\[[^]]*]").findAll(triggerId).map { result -> result.value }
            val text = arrays.fold(triggerId) { partial: String, array ->
                partial.replace(array, Regex(",.*").replace(array, ", ...]"))
            }

            return text
        }
    }

    interface MusicTriggerCompanion<TSelf: MusicTrigger<*>> {
        fun fromJson(json: JsonObject): TSelf {
            throw MusicTriggerException(
                "Type \"${this::class.qualifiedName}\" must define a fromJson function.")
        }
    }

    abstract class Parameters {
        fun getTriggerParams(): List<TriggerParam> {
            return ReflectionHelper.getConstructorParameterValues(this)
                .map { arg -> TriggerParam(arg.name, arg.value) }
        }

        fun initializeCopyFromArgs(vararg constructorArgs: Any): Parameters {
            return (this::class.primaryConstructor?.call(*constructorArgs) ?: default())
        }

        companion object: ParametersCompanion<Parameters> {
            override fun default(): Parameters {
                throw MusicTriggerException("default() called on abstract Parameters class.")
            }
        }

        interface ParametersCompanion<TSelf: Parameters> {
            fun default(): Parameters
        }
    }
}
