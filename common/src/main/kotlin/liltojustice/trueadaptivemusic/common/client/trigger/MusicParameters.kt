package liltojustice.trueadaptivemusic.common.client.trigger

import liltojustice.trueadaptivemusic.common.ReflectionHelper

abstract class MusicParameters {
    fun getMusicParams(): List<MusicParam> {
        return ReflectionHelper.getConstructorParameterValues(this)
            .map { arg -> MusicParam(arg.name, arg.value) }
    }

    companion object: ParametersCompanion<MusicParameters> {
        override fun default(): MusicParameters {
            throw MusicTriggerException("default() called on abstract Parameters class.")
        }
    }

    interface ParametersCompanion<TSelf: MusicParameters> {
        val displayNames: Map<String, String>
            get() = mapOf()

        val descriptions: Map<String, String>
            get() = mapOf()

        fun default(): TSelf
    }
}