package liltojustice.trueadaptivemusic.client.trigger

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.ReflectionHelper
import liltojustice.trueadaptivemusic.client.Serialize
import liltojustice.trueadaptivemusic.client.sound.SoundLibrary
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import net.minecraft.text.Text
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

abstract class MusicTrigger {
    @Serialize
    private val type = getTypeName()

    abstract fun getTypeName(): String

    fun getTriggerArgs(): List<TriggerArg> {
        return ReflectionHelper.getConstructorParameterValues(this)
            .map { arg -> TriggerArg(arg.name, arg.value) }
    }

    fun getTriggerId(): String {
        val args = getTriggerArgs()
        return getTypeName()  + if (args.isEmpty()) "" else "{${args.joinToString(",")}}"
    }

    companion object {
        fun getTruncatedTriggerId(triggerId: String): String {
            val arrays = Regex("\\[[^]]*]").findAll(triggerId).map { result -> result.value }
            val text = arrays.fold(triggerId) { partial: String, array ->
                partial.replace(array, Regex(",.*").replace(array, ", ...]"))
            }

            return text
        }
    }

    interface MusicTriggerCompanion {
        val displayName: String?
            get() = null

        val argDisplayNames: Map<String, String>
            get() = mapOf()

        val argDescriptions: Map<String, String>
            get() = mapOf()

        fun getDisplayName(triggerName: String): Text
        fun getArgDisplayName(triggerName: String, argName: String): Text?
        fun getArgDescription(triggerName: String, argName: String): Text?
    }

    abstract class Parameters {
        fun getTriggerParams(): List<TriggerParam> {
            return ReflectionHelper.getConstructorParameterValues(this)
                .map { arg -> TriggerParam(arg.name, arg.value) }
        }

        companion object: ParametersCompanion<Parameters> {
            override fun default(): Parameters {
                throw MusicTriggerException("default() called on abstract Parameters class.")
            }
        }

        interface ParametersCompanion<TSelf: Parameters> {
            val displayNames: Map<String, String>
                get() = mapOf()

            val descriptions: Map<String, String>
                get() = mapOf()

            fun default(): TSelf
        }
    }
}
