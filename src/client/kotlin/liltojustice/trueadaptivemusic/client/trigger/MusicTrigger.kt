package liltojustice.trueadaptivemusic.client.trigger

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.ReflectionHelper
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicTriggerException
import liltojustice.trueadaptivemusic.client.trigger.predicate.TriggerParam
import net.minecraft.util.JsonHelper
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
        fun fromJsonProvideRegistry(
            json: JsonObject,
            registry: MusicTriggerRegistry<out MusicTrigger>): MusicTrigger {
            val typeName = JsonHelper.getString(json, "type")
            val type = registry.getType(typeName)
            return (type.companionObject?.functions?.firstOrNull{ f -> f.name == "fromJson" }
                ?: throw MusicTriggerException("fromJson method missing."))
                .call(type.companionObjectInstance, json) as? MusicTrigger
                ?: throw MusicTriggerException("Could not instantiate music predicate from json")
        }

        fun getTruncatedTriggerId(triggerId: String): String {
            val arrays = Regex("\\[[^]]*]").findAll(triggerId).map { result -> result.value }
            val text = arrays.fold(triggerId) { partial: String, array ->
                partial.replace(array, Regex(",.*").replace(array, ", ...]"))
            }

            return text
        }

        override fun fromJson(json: JsonObject): MusicTrigger {
            throw MusicTriggerException("Cannot deserialize to MusicTrigger.")
        }

        override fun initializeFromArgs(typeName: String, vararg args: Any): MusicTrigger {
            throw MusicTriggerException("Cannot initialize MusicTrigger.")
        }
    }

    interface MusicTriggerCompanion<TSelf> where TSelf: MusicTrigger {
        fun fromJson(json: JsonObject): TSelf
        fun initializeFromArgs(typeName: String, vararg args: Any): TSelf
    }
}
