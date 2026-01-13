package liltojustice.trueadaptivemusic.client.trigger

import kotlin.reflect.KClass

abstract class MusicTriggerRegistry<T: MusicTrigger<*>> {
    private val nameToClass = HashMap<String, KClass<out T>>()
    private val classNameToName = HashMap<String, String>()

    fun getAll(): List<Map.Entry<String, KClass<out T>>> {
        return nameToClass.entries.sortedBy { entry -> entry.key }
    }

    fun getAllNames(): List<String> {
        return getAll().map { entry -> entry.key }
    }

    operator fun set(name: String, triggerType: Class<out T>) {
        this[name] = triggerType.kotlin
    }

    operator fun set(name: String, triggerType: KClass<out T>) {
        if (nameToClass.containsKey(name)) {
            throw MusicTriggerException("A trigger class with name \"${name}\" is already registered.")
        }

        val qualifiedName = triggerType.qualifiedName
            ?: throw MusicTriggerException("Provided trigger type is missing qualified name.")

        if (classNameToName.containsKey(qualifiedName)) {
            throw MusicTriggerException(
                "Duplicate trigger type from qualified name \"${qualifiedName}\" registered.")
        }

        nameToClass[name] = triggerType
        classNameToName[qualifiedName] = name
    }

    operator fun get(name: String): KClass<out T> {
        return nameToClass[name] ?: throw MusicTriggerException("Unknown trigger name \"$name\"")
    }

    operator fun get(triggerType: Class<out T>): String {
        return this[triggerType.kotlin]
    }

    operator fun get(triggerType: KClass<out T>): String {
        return classNameToName[triggerType.qualifiedName]
            ?: throw MusicTriggerException(
                "Unknown trigger type of qualified name \"${triggerType.qualifiedName}\"")
    }
}