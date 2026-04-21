package liltojustice.trueadaptivemusic.client.trigger

import liltojustice.trueadaptivemusic.ReflectionHelper
import liltojustice.trueadaptivemusicapi.trigger.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.TriggerState
import liltojustice.trueadaptivemusicapi.trigger.TriggerType
import net.minecraft.network.chat.Component
import kotlin.reflect.full.memberProperties

abstract class MusicTrigger<TType: TriggerType<TArg, TState>, TArg: TriggerArguments, TState: TriggerState>(
    val type: TType, val arguments: TArg, val state: TState) {

    companion object {
        fun getTruncatedTriggerId(triggerId: String): String {
            val arrays = Regex("\\[[^]]*]").findAll(triggerId).map { result -> result.value }
            val text = arrays.fold(triggerId) { partial: String, array ->
                partial.replace(array, Regex(",.*").replace(array, ", ...]"))
            }

            return text
        }

        inline fun <reified TArg: TriggerArguments> getTriggerArgs(arguments: TArg): List<TriggerArg> {
            return TArg::class.memberProperties.map { TriggerArg(it.name, it.get(arguments)) }
        }
    }

    interface MusicTriggerCompanion {
        fun getDisplayName(triggerName: String): Component
        fun getArgDisplayName(triggerName: String, argName: String): Component?
        fun getArgDescription(triggerName: String, argName: String): Component?
    }

    abstract class Parameters {
        fun getTriggerParams(): List<TriggerParam> {
            return ReflectionHelper.getConstructorParameterValues(this)
                .map { arg -> TriggerParam(arg.name, arg.value) }
        }

        companion object: ParametersCompanion<Parameters> {
            override fun default(): Parameters {
                throw MusicTriggerException("default() called on abstract Parameters class.")
            }
        }

        interface ParametersCompanion<TSelf: Parameters> {
            val displayNames: Map<String, String>
                get() = mapOf()

            val descriptions: Map<String, String>
                get() = mapOf()

            fun default(): TSelf
        }
    }
}
