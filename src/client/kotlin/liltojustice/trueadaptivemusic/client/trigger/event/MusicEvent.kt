package liltojustice.trueadaptivemusic.client.trigger.event

import liltojustice.trueadaptivemusic.ReflectionHelper
import liltojustice.trueadaptivemusic.client.Serialize
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.text.translatableWithFallbackOrNull
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import liltojustice.trueadaptivemusic.client.trigger.TriggerReflectionHelper
import liltojustice.trueadaptivemusic.text.StringExtensions.prettify
import net.minecraft.text.Text
import kotlin.collections.plus
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.primaryConstructor

abstract class MusicEvent: MusicTrigger() {
    @Serialize
    var music: List<PlayableSound> = listOf()

    @Serialize
    var parameters = Parameters.default()

    open fun validate(vararg eventArgs: Any?): Boolean {
        return true
    }

    final override fun getTypeName(): String {
        return if (this is ErrorEvent)
            ErrorEvent.NAME
        else
            TAMClient.eventRegistry[this::class]
    }

    companion object: MusicEventCompanion {
    }

    data class Parameters(var isPersistent: Boolean = false): MusicTrigger.Parameters() {
        companion object: ParametersCompanion<Parameters> {
            override val displayNames: Map<String, String>
                get() = super.displayNames +
                        Parameters::class.declaredMembers.map { it.name }.associateWith { it.prettify() }

            override val descriptions: Map<String, String>
                get() = super.descriptions + mapOf(
                    "isPersistent" to "Don't stop this event's music after leaving this predicate.")

            override fun default(): Parameters {
                return Parameters()
            }

            fun fromArgs(paramArgs: List<Any>): Parameters {
                return Parameters::class.primaryConstructor?.call(*paramArgs.toTypedArray()) ?: default()
            }

            fun getParamDisplayName(paramName: String): Text? {
                return translatableWithFallbackOrNull(
                    "trueadaptivemusic.param.event.${paramName}.display", displayNames[paramName])
            }

            fun getParamDescription(paramName: String): Text? {
                return Text.translatableWithFallback(
                    "trueadaptivemusic.param.event.${paramName}.description", descriptions[paramName])
            }
        }
    }

    interface MusicEventCompanion: MusicTriggerCompanion {
        override fun getDisplayName(triggerName: String): Text {
            return Text.translatableWithFallback(
                "trueadaptivemusic.event.name.${triggerName}",
                displayName ?: triggerName.prettify()
            )
        }

        override fun getArgDisplayName(triggerName: String, argName: String): Text? {
            val eventType = TAMClient.eventRegistry[triggerName]
            val inferredDisplayNames = ReflectionHelper.getConstructorParameterNames(eventType)
            val combined = inferredDisplayNames.associateWith { it.prettify() } +
                    TriggerReflectionHelper.getMusicTriggerArgDisplayNames(eventType)
            return translatableWithFallbackOrNull(
                "trueadaptivemusic.event.arg.${triggerName}.${argName}.display",
                combined[argName]
            )
        }

        override fun getArgDescription(triggerName: String, argName: String): Text? {
            return translatableWithFallbackOrNull(
                "trueadaptivemusic.event.arg.${triggerName}.${argName}.description",
                TriggerReflectionHelper.getMusicTriggerArgDescriptions(
                    TAMClient.eventRegistry[triggerName])[argName])
        }
    }
}