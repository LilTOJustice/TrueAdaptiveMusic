package liltojustice.trueadaptivemusic.client

import org.reflections.Reflections
import kotlin.reflect.KClass

class ReflectionHelper {
    companion object {
        private val reflections = Reflections()

        fun <T: Any> getSubclassesOf(parentClass: KClass<T>): List<KClass<out T>> {
            return reflections.getSubTypesOf(parentClass.java).map { childClass -> childClass.kotlin }
        }
    }
}