package liltojustice.trueadaptivemusic

import org.reflections.Reflections
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

class ReflectionHelper {
    companion object {
        private val reflections = Reflections()

        fun <T: Any> getSubclassesOf(parentClass: KClass<T>): List<KClass<out T>> {
            return reflections.getSubTypesOf(parentClass.java).map { childClass -> childClass.kotlin }
        }

        fun getConstructorParameterValues(instance: Any): List<ParameterValue> {
            val constructor = instance::class.primaryConstructor
                ?: throw ReflectionHelperException("No constructor found for ${instance::class.simpleName}." +
                        " It must have a constructor.")
            val result = instance::class.declaredMemberProperties
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
    }

    class ReflectionHelperException(message: String? = null): Exception(message)

    data class ParameterValue(val name: String, val value: Any?)
}