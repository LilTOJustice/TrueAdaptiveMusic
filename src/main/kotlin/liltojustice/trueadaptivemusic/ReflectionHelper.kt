package liltojustice.trueadaptivemusic

import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

class ReflectionHelper {
    companion object {
        fun getConstructorParameterValues(instance: Any): List<ParameterValue> {
            val constructor = instance::class.primaryConstructor
                ?: throw ReflectionHelperException("No constructor found for ${instance::class.simpleName}." +
                        " It must have a constructor.")
            val fieldMap = instance.javaClass.declaredFields.withIndex().associate { p -> p.value.name to p.index }
            val result = instance::class.declaredMemberProperties.sortedBy { member -> fieldMap[member.name] }
                .filter { property -> constructor.parameters.any { param -> property.name == param.name } }
                .map { property ->
                    val accessible = property.isAccessible
                    property.isAccessible = true
                    val value = property.getter.call(instance)
                    property.isAccessible = accessible
                    ParameterValue(property.name, value)
                }

            if (result.size < constructor.parameters.size) {
                throw ReflectionHelperException("Couldn't read all expected parameters for ${this::class.simpleName}." +
                        " Make sure all arguments to its primary constructor are declared properties.")
            }

            return result
        }

        fun getConstructorParameterNames(clazz: KClass<*>): List<String> {
            val constructor = clazz.primaryConstructor
                ?: throw ReflectionHelperException("No constructor found for ${clazz.simpleName}." +
                        " It must have a constructor.")
            val declaredFields = clazz.java.declaredFields.map { it.name }
            return constructor.parameters.mapNotNull { it.name }.filter { declaredFields.contains(it) }
        }
    }

    class ReflectionHelperException(message: String? = null): Exception(message)

    data class ParameterValue(val name: String, val value: Any?)
}