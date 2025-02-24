package liltojustice.trueadaptivemusic.client.predicate

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import net.minecraft.client.MinecraftClient
import net.minecraft.util.JsonHelper
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions
import kotlin.reflect.full.primaryConstructor

sealed class MusicPredicate {

    abstract fun test(client: MinecraftClient): Boolean
    abstract fun getIDs(): List<String>

    fun toJson(): JsonObject {
        val result = JsonObject()
        result.add("type", JsonPrimitive(getTypeName()))
        result.add("id", JsonPrimitive(getIDs().firstOrNull() ?: ""))

        return result
    }

    fun getPredicateId(): String {
        val companion = javaClass.kotlin.companionObjectInstance
        if (companion is MusicPredicateCompanion<*>)
        {
            return "${companion.getTypeName()}{${getIDs().joinToString(",")}}"
        } else throw MusicPredicateException("Failed to find valid companion object for $javaClass make sure to" +
                " create one that inherits from MusicPredicateCompanion")
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
            return getConstructorFromTypeName(typeName)?.parameters
                ?: throw MusicPredicateException("Failed to get type parameters for constructing $typeName.")
        }

        fun initializeFromArgs(typeName: String, vararg args: Any): MusicPredicate {
            return getConstructorFromTypeName(typeName)?.call(*args)
                ?: throw MusicPredicateException("Initialization of MusicPredicate type $typeName failed.")
        }

        private fun getConstructorFromTypeName(typeName: String): KFunction<MusicPredicate>? {
            return MusicPredicate::class.sealedSubclasses.firstOrNull { subclass ->
                subclass.companionObject?.functions?.firstOrNull { f ->
                    f.name == "getTypeName" }?.call(subclass.companionObjectInstance) == typeName }
                ?.primaryConstructor
        }
    }

    interface MusicPredicateCompanion<TSelf> where TSelf: MusicPredicate {
        fun getTypeName(): String
        fun fromJson(json: JsonObject): TSelf
    }
}