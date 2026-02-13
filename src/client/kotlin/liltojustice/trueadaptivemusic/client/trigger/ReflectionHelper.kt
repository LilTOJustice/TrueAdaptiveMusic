package liltojustice.trueadaptivemusic.client.trigger

import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

object ReflectionHelper {
    fun getMusicTriggerArgDescriptions(clazz: KClass<out MusicTrigger<*>>): Map<String, String> {
        return (clazz.companionObjectInstance as? MusicTrigger.MusicTriggerCompanion<*>)?.descriptions ?: mapOf()
    }
}