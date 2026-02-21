package liltojustice.trueadaptivemusic.client.trigger.predicate

import liltojustice.trueadaptivemusic.ReflectionHelper
import liltojustice.trueadaptivemusic.client.Serialize
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.text.translatableWithFallbackOrNull
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import liltojustice.trueadaptivemusic.client.trigger.TriggerReflectionHelper
import liltojustice.trueadaptivemusic.text.StringExtensions.prettify
import net.minecraft.text.Text
import kotlin.reflect.full.declaredMembers

abstract class MusicPredicate: MusicTrigger<MusicPredicate.Parameters>() {
    init {
        parameters = Parameters.default()
    }

    private var lastResult = false
    private var ticksSinceResult = getFixedTickRate()

    @Serialize
    var ambience = listOf<PlayableSound>()

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

    data class Parameters(
        var trackDelay: UInt = 0U,
        var trackDelayNoise: UInt = 0U,
        var enterDelay: UInt = 0U,
        var inheritMusic: Boolean = false,
        var inheritAmbience: Boolean = true)
        : MusicTrigger.Parameters() {
        companion object: ParametersCompanion<Parameters> {
            override val displayNames: Map<String, String>
                get() = super.displayNames +
                        Parameters::class.declaredMembers.map { it.name }.associateWith { it.prettify() }

            override val descriptions: Map<String, String>
                get() = super.descriptions + mapOf(
                    "trackDelay" to "After a track finishes, wait this many seconds before playing the next.",
                    "trackDelayNoise" to "Add randomly + or - this many seconds to track delay.",
                    "enterDelay" to "Wait this many seconds before starting music when entering this predicate. " +
                            "Disables music resuming for this predicate.",
                    "inheritMusic" to "Include this predicate's parent's music along with this predicate's music.",
                    "inheritAmbience" to "Include this predicate's parent's ambience along with this predicate's " +
                            "ambience.")

            override fun default(): Parameters {
                return Parameters()
            }

            fun getParamDisplayName(paramName: String): Text? {
                return translatableWithFallbackOrNull(
                    "trueadaptivemusic.param.predicate.${paramName}.display", displayNames[paramName])
            }

            fun getParamDescription(paramName: String): Text? {
                return Text.translatableWithFallback(
                    "trueadaptivemusic.param.predicate.${paramName}.description", descriptions[paramName])
            }
        }
    }

    companion object: MusicPredicateCompanion<MusicPredicate> {
    }

    interface MusicPredicateCompanion<TSelf>: MusicTriggerCompanion where TSelf: MusicPredicate {
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