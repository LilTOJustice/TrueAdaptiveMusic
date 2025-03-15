package liltojustice.trueadaptivemusic.client.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicateException
import liltojustice.trueadaptivemusic.client.predicate.PredicateParam
import net.minecraft.client.MinecraftClient
import net.minecraft.util.JsonHelper
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible

sealed class MusicPredicate {
    abstract fun test(client: MinecraftClient): Boolean

    fun getPredicateParams(): List<PredicateParam> {
        val constructor = this::class.primaryConstructor
            ?: throw MusicPredicateException("No constructor found for ${this::class.simpleName}." +
                    " It must have a constructor.")
        val result = this::class.declaredMemberProperties
            .filter { property -> constructor.parameters.any { param -> property.name == param.name } }
            .map { property ->
                val accessible = property.isAccessible
                property.isAccessible = true
                val value = property.getter.call(this)
                property.isAccessible = accessible
                PredicateParam(property.name, value)
            }

        if (result.size < constructor.parameters.size) {
            throw MusicPredicateException("Couldn't read all expected parameters for ${this::class.simpleName}." +
                    " Make sure all arguments to its primary constructor are declared properties.")
        }

        return result
    }

    open fun toJson(): JsonObject {
        val result = JsonObject()
        result.addProperty("type", getTypeName())

        return result
    }

    fun getPredicateId(): String {
        val companion = javaClass.kotlin.companionObjectInstance
        if (companion is MusicPredicateCompanion<*>) {
            val params = getPredicateParams()
            return companion.getTypeName() + if (params.isEmpty()) "" else "{${params.joinToString(",")}}"
        }
        else {
            throw MusicPredicateException("Failed to find valid companion object for $javaClass make sure to" +
                    " create one that inherits from ${MusicPredicateCompanion::class.simpleName}")
        }
    }

    fun getTypeName(): String {
        val companion = javaClass.kotlin.companionObjectInstance
        if (companion is MusicPredicateCompanion<*>) {
            return companion.getTypeName()
        } else throw MusicPredicateException("Failed to find valid companion object for $javaClass make sure to" +
            " create one that inherits from ${MusicPredicateCompanion::class.simpleName}")
    }

    companion object: MusicPredicateCompanion<MusicPredicate> {
        override fun getTypeName(): String {
            throw MusicPredicateException("Attempt to get type name from abstract predicate type.")
        }

        override fun fromJson(json: JsonObject): MusicPredicate {
            val type = JsonHelper.getString(json, "type")
            for (subclass in MusicPredicate::class.sealedSubclasses)
            {
                if ((subclass.companionObject?.functions?.firstOrNull{ f -> f.name == "getTypeName" }
                    ?: throw MusicPredicateException("Invalid music predicate type: $type"))
                        .call(subclass.companionObjectInstance) == type)
                {
                    return (subclass.companionObject?.functions?.firstOrNull{ f -> f.name == "fromJson" }
                        ?: throw MusicPredicateException("fromJson method missing."))
                        .call(subclass.companionObjectInstance, json)
                            as? MusicPredicate
                        ?: throw MusicPredicateException("Could not instantiate music predicate from json")
                }
            }

            throw MusicPredicateException("Invalid music predicate type: $type")
        }

        fun getTypeNames(): List<String> {
            return MusicPredicate::class.sealedSubclasses.mapNotNull { subclass ->
                subclass.companionObject?.functions?.firstOrNull { f ->
                    f.name == "getTypeName"
                }?.call(subclass.companionObjectInstance) as? String
            }
        }

        fun getRequiredArgsFromTypeName(typeName: String): List<KParameter> {
            return getConstructorFromTypeName(typeName).parameters
        }

        fun initializeFromArgs(typeName: String, vararg args: Any): MusicPredicate {
            return getConstructorFromTypeName(typeName).call(*args)
        }

        private fun getConstructorFromTypeName(typeName: String): KFunction<MusicPredicate> {
            return MusicPredicate::class.sealedSubclasses.firstOrNull { subclass ->
                subclass.companionObject?.functions?.firstOrNull { f ->
                    f.name == "getTypeName" }?.call(subclass.companionObjectInstance) == typeName }
                ?.primaryConstructor
                ?: throw MusicPredicateException("No constructor found for $typeName. It must have a constructor.")
        }
    }

    interface MusicPredicateCompanion<TSelf> where TSelf: MusicPredicate {
        fun getTypeName(): String
        fun fromJson(json: JsonObject): TSelf
    }
}