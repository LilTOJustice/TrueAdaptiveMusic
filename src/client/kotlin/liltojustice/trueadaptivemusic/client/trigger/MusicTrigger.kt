package liltojustice.trueadaptivemusic.client.trigger

import liltojustice.trueadaptivemusicapi.trigger.TriggerTypeBase
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.state.TriggerState
import net.minecraft.network.chat.Component
import kotlin.reflect.full.memberProperties

abstract class MusicTrigger<TTrigger: TriggerTypeBase>(
    val type: TTrigger, val arguments: TriggerArguments, val state: TriggerState) {

    companion object {
        fun getTruncatedTriggerId(triggerId: String): String {
            val arrays = Regex("\\[[^]]*]").findAll(triggerId).map { result -> result.value }
            val text = arrays.fold(triggerId) { partial: String, array ->
                partial.replace(
                    array, Regex(",.*").replace(array, ", ...]"))
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
}
