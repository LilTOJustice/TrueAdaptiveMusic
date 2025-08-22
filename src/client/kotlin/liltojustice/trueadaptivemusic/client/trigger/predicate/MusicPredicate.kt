package liltojustice.trueadaptivemusic.client.trigger.predicate

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import net.minecraft.client.MinecraftClient
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

abstract class MusicPredicate: MusicTrigger {
    abstract fun test(client: MinecraftClient): Boolean

    override fun getTypeName(): String {
        return if (this is ErrorPredicate)
            ErrorPredicate.NAME
        else
            MusicPredicateRegistry[this::class]
    }

    companion object: MusicPredicateCompanion<MusicPredicate> {
        override fun fromJson(json: JsonObject): MusicPredicate {
            return try {
                MusicTrigger.fromJsonProvideRegistry(json, MusicPredicateRegistry)
            } catch (e: MusicTriggerException) {
                ErrorPredicate(json, e.message ?: "Unknown")
            } as MusicPredicate
        }
    }

    interface MusicPredicateCompanion<TSelf>: MusicTrigger.MusicTriggerCompanion<MusicPredicate>
            where TSelf: MusicPredicate {
        fun getRequiredArgsFromTypeName(typeName: String): List<KParameter> {
            return if (typeName == ErrorPredicate.NAME) emptyList() else getConstructorFromTypeName(typeName).parameters
        }

        private fun getConstructorFromTypeName(typeName: String): KFunction<Any> {
            return MusicPredicateRegistry[typeName].primaryConstructor
                ?: throw MusicTriggerException(
                    "Trigger type with name \"$typeName\" has no primary constructor.")
        }

        override fun initializeFromArgs(typeName: String, vararg args: Any): MusicPredicate {
            return getConstructorFromTypeName(typeName).call(*args) as MusicPredicate
        }
    }
}