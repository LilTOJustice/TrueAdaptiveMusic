package liltojustice.trueadaptivemusic.client.trigger.predicate

import liltojustice.trueadaptivemusic.ReflectionHelper
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.text.translatableWithFallbackOrNull
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import liltojustice.trueadaptivemusic.client.trigger.TriggerReflectionHelper
import liltojustice.trueadaptivemusic.text.StringExtensions.prettify
import net.minecraft.text.Text

abstract class MusicPredicate: MusicTrigger() {
    private var lastResult = false
    private var ticksSinceResult = getFixedTickRate()

    protected abstract fun test(): Boolean

    final override fun getTypeName(): String {
        return if (this is ErrorPredicate)
            ErrorPredicate.NAME
        else
            TAMClient.predicateRegistry[this::class]
    }

    fun testPredicate(): Boolean {
        val tickRate = getFixedTickRate()
        if (ticksSinceResult++ == tickRate) {
            ticksSinceResult = 1

            lastResult = test()
        }

        return lastResult
    }

    open fun getTickRate(): Int {
        return 20
    }

    private fun getFixedTickRate(): Int {
        val desiredTickRate = getTickRate()
        return if (desiredTickRate < 1) 0 else desiredTickRate
    }


    companion object: MusicPredicateCompanion {
    }

    interface MusicPredicateCompanion: MusicTriggerCompanion {
        override fun getDisplayName(triggerName: String): Text {
            return Text.translatableWithFallback(
                "trueadaptivemusic.predicate.name.${triggerName}",
                displayName ?: triggerName.prettify()
            )
        }

        override fun getArgDisplayName(triggerName: String, argName: String): Text? {
            val predicateType = TAMClient.predicateRegistry[triggerName]
            val inferredDisplayNames = ReflectionHelper.getConstructorParameterNames(predicateType)
            val combined = inferredDisplayNames.associateWith { it.prettify() } +
                TriggerReflectionHelper.getMusicTriggerArgDisplayNames(predicateType)

            return translatableWithFallbackOrNull(
                "trueadaptivemusic.predicate.arg.${triggerName}.${argName}.display",
                combined[argName]
            )
        }

        override fun getArgDescription(triggerName: String, argName: String): Text? {
            return translatableWithFallbackOrNull(
                "trueadaptivemusic.predicate.arg.${triggerName}.${argName}.description",
                TriggerReflectionHelper.getMusicTriggerArgDescriptions(
                    TAMClient.predicateRegistry[triggerName])[argName]
            )
        }
    }
}