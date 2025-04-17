package liltojustice.trueadaptivemusic.client.trigger

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.ReflectionHelper
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicateException
import liltojustice.trueadaptivemusic.client.trigger.predicate.TriggerParam
import net.minecraft.util.JsonHelper
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
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
        val companion = javaClass.kotlin.companionObjectInstance
        if (companion is MusicTriggerCompanion<*>) {
            val params = getTriggerParams()
            return companion.getTypeName() + if (params.isEmpty()) "" else "{${params.joinToString(",")}}"
        }
        else {
            throw MusicPredicateException(getMissingCompanionExceptionText(javaClass.kotlin))
        }
    }

    fun getTypeName(): String {
        val companion = javaClass.kotlin.companionObjectInstance
        if (companion is MusicTriggerCompanion<*>) {
            return companion.getTypeName()
        } else {
            throw MusicPredicateException(getMissingCompanionExceptionText(javaClass.kotlin))
        }
    }

    companion object: MusicTriggerCompanion<MusicTrigger> {
        fun fromJsonProvideSubclasses(
            json: JsonObject,
            subclasses: List<KClass<out MusicTrigger>> = getTriggerImplementerSubclasses()
        )
        : MusicTrigger {
            val type = JsonHelper.getString(json, "type")
            for (subclass in subclasses)
            {
                if ((subclass.companionObject?.functions?.firstOrNull{ f -> f.name == "getTypeName" }
                        ?: throw MusicPredicateException(getMissingCompanionExceptionText(subclass)))
                        .call(subclass.companionObjectInstance) == type)
                {
                    return (subclass.companionObject?.functions?.firstOrNull{ f -> f.name == "fromJson" }
                        ?: throw MusicPredicateException("fromJson method missing."))
                        .call(subclass.companionObjectInstance, json) as? MusicTrigger
                        ?: throw MusicPredicateException("Could not instantiate music predicate from json")
                }
            }

            throw MusicPredicateException("Invalid music predicate type: $type")
        }

        override fun getTriggerImplementerSubclasses(): List<KClass<out MusicTrigger>> {
            return ReflectionHelper.getSubclassesOf(MusicTrigger::class)
        }

        override fun getTypeName(): String {
            throw MusicPredicateException("Attempt to get type name from MusicTrigger interface.")
        }

        override fun fromJson(json: JsonObject): MusicTrigger {
            return fromJsonProvideSubclasses(json)
        }

        private fun getMissingCompanionExceptionText(offendingClass: KClass<out MusicTrigger>): String {
            return "Failed to find valid companion object for ${offendingClass.simpleName}. make sure to create one " +
                    "that inherits from ${offendingClass.superclasses.first().companionObject!!.qualifiedName}"
        }
    }

    interface MusicTriggerCompanion<TSelf> where TSelf: MusicTrigger {
        fun getTriggerImplementerSubclasses(): List<KClass<out TSelf>>
        fun getTypeName(): String
        fun fromJson(json: JsonObject): TSelf

        fun getTypeNames(): List<String> {
            return getTriggerImplementerSubclasses().mapNotNull { subclass ->
                subclass.companionObject?.functions?.firstOrNull { f ->
                    f.name == "getTypeName"
                }?.call(subclass.companionObjectInstance) as? String
            }
        }

        fun getRequiredArgsFromTypeName(typeName: String): List<KParameter> {
            return getConstructorFromTypeName(typeName).parameters
        }

        fun initializeFromArgs(typeName: String, vararg args: Any): TSelf {
            return getConstructorFromTypeName(typeName).call(*args)
        }

        fun getConstructorFromTypeName(typeName: String): KFunction<TSelf> {
            return getTriggerImplementerSubclasses().firstOrNull { subclass ->
                subclass.companionObject?.functions?.firstOrNull { f ->
                    f.name == "getTypeName" }?.call(subclass.companionObjectInstance) == typeName }
                ?.primaryConstructor
                ?: throw MusicPredicateException("No constructor found for $typeName. It must have a constructor.")
        }
    }
}
