package liltojustice.trueadaptivemusic.client.trigger.predicate

import liltojustice.trueadaptivemusic.text.translatableWithFallbackOrNull
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import liltojustice.trueadaptivemusic.text.StringExtensions.prettify
import liltojustice.trueadaptivemusicapi.TAMAPI
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.PredicateTypeBase
import liltojustice.trueadaptivemusicapi.trigger.state.TriggerState
import net.minecraft.network.chat.Component

class MusicPredicate<TTrigger: PredicateTypeBase>(type: TTrigger, arguments: TriggerArguments, state: TriggerState)
    : MusicTrigger<TTrigger>(type, arguments, state) {
    private var lastResult = false
    private var ticksSinceResult = 0

    fun test(): Boolean {
        val tickRate = getFixedTickRate()
        if (ticksSinceResult == 0 || ticksSinceResult >= tickRate) {
            ticksSinceResult = 1

            lastResult = type.testBase(arguments, state)
        }

        ticksSinceResult++

        return lastResult
    }

    private fun getFixedTickRate(): Int {
        return if (type.tickRate < 1) 1 else type.tickRate
    }

    companion object: MusicPredicateCompanion

    interface MusicPredicateCompanion: MusicTriggerCompanion {
        override fun getDisplayName(triggerName: String): Component {
            return Component.translatableWithFallback(
                "trueadaptivemusic.predicate.name.${triggerName}",
                TAMAPI.getPredicateType(triggerName)?.displayName ?: triggerName.prettify()
            )
        }

        override fun getArgDisplayName(triggerName: String, argName: String): Component? {
            val predicateType = TAMAPI.getPredicateType(triggerName) ?: return null

            return translatableWithFallbackOrNull(
                "trueadaptivemusic.predicate.arg.${triggerName}.${argName}.display",
                predicateType.argDisplayNames[argName]
            )
        }

        override fun getArgDescription(triggerName: String, argName: String): Component? {
            val predicateType = TAMAPI.getPredicateType(triggerName) ?: return null

            return translatableWithFallbackOrNull(
                "trueadaptivemusic.predicate.arg.${triggerName}.${argName}.description",
                predicateType.argDescriptions[argName]
            )
        }
    }
}