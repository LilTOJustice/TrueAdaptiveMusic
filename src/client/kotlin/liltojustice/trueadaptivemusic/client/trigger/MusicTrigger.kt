package liltojustice.trueadaptivemusic.client.trigger

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.ReflectionHelper
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicTriggerException
import liltojustice.trueadaptivemusic.client.trigger.predicate.TriggerParam
import net.minecraft.util.JsonHelper
import kotlin.reflect.KClass
import kotlin.reflect.full.*

interface MusicTrigger {
    fun getTriggerParams(): List<TriggerParam> {
        return ReflectionHelper.getConstructorParameterValues(this)
            .map { param -> TriggerParam(param.name, param.value) }
    }

    fun toJson(): JsonObject {
        val result = JsonObject()
        result.addProperty("type", getTypeName())

        return result
    }

    fun getTriggerId(): String {
        val params = getTriggerParams()
        return getTypeName() + if (params.isEmpty()) "" else "{${params.joinToString(",")}}"
    }

    fun getTypeName(): String

    companion object: MusicTriggerCompanion<MusicTrigger> {
        fun fromJsonProvideSubclasses(
            json: JsonObject,
            subclasses: List<KClass<out MusicTrigger>>): MusicTrigger {
            val type = JsonHelper.getString(json, "type")
            for (subclass in subclasses)
            {
                if ((subclass.companionObject?.functions?.firstOrNull{ f -> f.name == "getTypeName" }
                        ?: throw MusicTriggerException(getMissingCompanionExceptionText(subclass)))
                        .call(subclass.companionObjectInstance) == type)
                {
                    return (subclass.companionObject?.functions?.firstOrNull{ f -> f.name == "fromJson" }
                        ?: throw MusicTriggerException("fromJson method missing."))
                        .call(subclass.companionObjectInstance, json) as? MusicTrigger
                        ?: throw MusicTriggerException("Could not instantiate music predicate from json")
                }
            }

            throw MusicTriggerException("Unknown music trigger type: $type")
        }

        fun getTruncatedTriggerId(triggerId: String): String {
            val arrays = Regex("\\[[^]]*]").findAll(triggerId).map { result -> result.value }
            val text = arrays.fold(triggerId) { partial: String, array ->
                partial.replace(array, Regex(",.*").replace(array, ", ...]"))
            }

            return text
        }

        override fun fromJson(json: JsonObject): MusicTrigger {
            return fromJsonProvideSubclasses(json, emptyList())
        }

        override fun initializeFromArgs(typeName: String, vararg args: Any): MusicTrigger {
            throw MusicTriggerException("Cannot initialize MusicTrigger.")
        }

        private fun getMissingCompanionExceptionText(offendingClass: KClass<out MusicTrigger>): String {
            return "Failed to find valid companion object for ${offendingClass.simpleName}. make sure to create one " +
                    "that inherits from ${offendingClass.superclasses.first().companionObject!!.qualifiedName}"
        }
    }

    interface MusicTriggerCompanion<TSelf> where TSelf: MusicTrigger {
        fun fromJson(json: JsonObject): TSelf
        fun initializeFromArgs(typeName: String, vararg args: Any): TSelf
    }
}
