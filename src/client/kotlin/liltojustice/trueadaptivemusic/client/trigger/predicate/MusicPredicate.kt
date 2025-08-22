package liltojustice.trueadaptivemusic.client.trigger.predicate

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import liltojustice.trueadaptivemusic.client.trigger.MusicTriggerRegistry
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import net.minecraft.client.MinecraftClient
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

abstract class MusicPredicate: MusicTrigger {
    abstract fun test(client: MinecraftClient): Boolean

    override fun getTypeName(): String {
        return if (this is ErrorPredicate)
            ErrorPredicate.NAME
        else
            registry.getTypeName(this::class)
    }

    companion object: MusicPredicateCompanion<MusicPredicate> {
        private val registry = MusicTriggerRegistry<MusicPredicate>()

        fun register(name: String, type: Class<MusicPredicate>) {
            registry.register(name, type.kotlin)
        }

        fun register(name: String, type: KClass<MusicPredicate>) {
            registry.register(name, type)
        }

        fun getTypeNames(): List<String> {
            return registry.getAll().map { entry -> entry.key }
        }

        fun getNameFromType(type: KClass<out MusicPredicate>): String {
            return registry.getTypeName(type)
        }

        fun getNameFromType(type: Class<out MusicEvent>): String {
            return MusicEvent.Companion.getNameFromType(type.kotlin)
        }

        override fun fromJson(json: JsonObject): MusicPredicate {
            return try {
                MusicTrigger
                    .fromJsonProvideSubclasses(json, registry.getAll().map { entry -> entry.value })
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

        fun getConstructorFromTypeName(typeName: String): KFunction<Any> {
            return registry.getType(typeName)::class.primaryConstructor
                ?: throw MusicTriggerException(
                    "Trigger type with name \"$typeName\" has no primary constructor.")
        }

        override fun initializeFromArgs(typeName: String, vararg args: Any): MusicPredicate {
            return registry.getType(typeName)::class.primaryConstructor?.call(*args) as MusicPredicate
        }
    }
}