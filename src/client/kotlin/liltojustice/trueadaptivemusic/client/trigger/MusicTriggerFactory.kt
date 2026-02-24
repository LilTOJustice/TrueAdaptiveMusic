package liltojustice.trueadaptivemusic.client.trigger

import liltojustice.trueadaptivemusic.client.trigger.predicate.ErrorPredicate
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

@Suppress("UNCHECKED_CAST")
abstract class MusicTriggerFactory<T: MusicTrigger> (private val registry: MusicTriggerRegistry<T>) {
    fun fromArgs(typeName: String, args: List<Any>): T {
        return getConstructorFromTypeName(typeName).call(*args.toTypedArray()) as? T
            ?: throw MusicTriggerException("Could not instantiate music trigger from args.")
    }

    fun getRequiredArgs(typeName: String): List<KParameter> {
        return if (typeName == ErrorPredicate.NAME) emptyList() else getConstructorFromTypeName(typeName).parameters
    }

    private fun getConstructorFromTypeName(typeName: String): KFunction<Any> {
        return registry[typeName].primaryConstructor
            ?: throw MusicTriggerException(
                "Trigger type with name \"$typeName\" has no primary constructor.")
    }
}