package liltojustice.trueadaptivemusic.client.music

import liltojustice.trueadaptivemusic.ReflectionHelper
import liltojustice.trueadaptivemusic.client.trigger.MusicTriggerException
import liltojustice.trueadaptivemusic.client.trigger.TriggerArg
import liltojustice.trueadaptivemusic.client.trigger.TriggerParam

abstract class MusicTriggerParameters {
    fun getTriggerParams(): List<TriggerParam> {
        return ReflectionHelper.getConstructorParameterValues(this)
            .map { arg -> TriggerParam(arg.name, arg.value) }
    }

    companion object: ParametersCompanion<MusicTriggerParameters> {
        override fun default(): MusicTriggerParameters {
            throw MusicTriggerException("default() called on abstract Parameters class.")
        }
    }

    interface ParametersCompanion<TSelf: MusicTriggerParameters> {
        val displayNames: Map<String, String>
            get() = mapOf()

        val descriptions: Map<String, String>
            get() = mapOf()

        fun default(): TSelf
    }
}