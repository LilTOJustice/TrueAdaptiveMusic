package liltojustice.trueadaptivemusic.client.trigger.event

import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.text.translatableWithFallbackOrNull
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import liltojustice.trueadaptivemusic.client.trigger.MusicParameters
import liltojustice.trueadaptivemusic.text.StringExtensions.prettify
import liltojustice.trueadaptivemusicapi.TAMAPI
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.event.input.EventInput
import liltojustice.trueadaptivemusicapi.trigger.event.type.EventTypeBase
import liltojustice.trueadaptivemusicapi.trigger.state.TriggerState
import net.minecraft.text.Text
import kotlin.collections.plus
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.primaryConstructor

class MusicEvent<TEvent: EventTypeBase>(
    type: TEvent,
    arguments: TriggerArguments,
    state: TriggerState,
    var music: List<PlayableSound> = emptyList(),
    var parameters: Parameters = Parameters.default()
): MusicTrigger<TEvent>(type, arguments, state) {
    fun validate(input: EventInput): Boolean {
        return type.validateBase(arguments, state, input)
    }

    companion object: MusicEventCompanion

    data class Parameters(var isPersistent: Boolean = false): MusicParameters() {
        companion object: ParametersCompanion<Parameters> {
            override val displayNames: Map<String, String>
                get() = super.displayNames +
                        Parameters::class.declaredMembers.map { it.name }.associateWith { it.prettify() }

            override val descriptions: Map<String, String>
                get() = super.descriptions + mapOf(
                    Parameters::isPersistent.name to "Don't stop this event's music after leaving this predicate.")

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
                TAMAPI.getEventType(triggerName)?.displayName ?: triggerName.prettify()
            )
        }

        override fun getArgDisplayName(triggerName: String, argName: String): Text? {
            val eventType = TAMAPI.getEventType(triggerName) ?: return null

            return translatableWithFallbackOrNull(
                "trueadaptivemusic.event.arg.${triggerName}.${argName}.display",
                eventType.argDisplayNames[argName]
            )
        }

        override fun getArgDescription(triggerName: String, argName: String): Text? {
            val eventType = TAMAPI.getEventType(triggerName) ?: return null

            return translatableWithFallbackOrNull(
                "trueadaptivemusic.event.arg.${triggerName}.${argName}.description",
                eventType.argDescriptions[argName]
            )
        }
    }
}