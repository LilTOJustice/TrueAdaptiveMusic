package liltojustice.trueadaptivemusic.client.trigger.event

import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.text.translatableWithFallbackOrNull
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import liltojustice.trueadaptivemusic.client.music.MusicTriggerParameters
import liltojustice.trueadaptivemusic.text.StringExtensions.prettify
import liltojustice.trueadaptivemusicapi.TAMAPI
import liltojustice.trueadaptivemusicapi.trigger.event.arguments.EventArguments
import liltojustice.trueadaptivemusicapi.trigger.event.input.EventInput
import liltojustice.trueadaptivemusicapi.trigger.event.state.EventState
import liltojustice.trueadaptivemusicapi.trigger.event.type.EventType
import net.minecraft.network.chat.Component
import kotlin.collections.plus
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.primaryConstructor

class MusicEvent<
        TTrigger: EventType<TArg, TState, TInput>,
        TArg: EventArguments,
        TState: EventState,
        TInput: EventInput>(
    type: TTrigger,
    arguments: TArg,
    state: TState,
    var music: List<PlayableSound> = emptyList(),
    var parameters: Parameters = Parameters.default()
): MusicTrigger<TTrigger, TArg, TState>(type, arguments, state) {
    fun validateEvent(input: TInput): Boolean {
        return type.validate(arguments, state, input)
    }

    companion object: MusicEventCompanion

    data class Parameters(var isPersistent: Boolean = false): MusicTriggerParameters() {
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

            fun getParamDisplayName(paramName: String): Component? {
                return translatableWithFallbackOrNull(
                    "trueadaptivemusic.param.event.${paramName}.display", displayNames[paramName])
            }

            fun getParamDescription(paramName: String): Component {
                return Component.translatableWithFallback(
                    "trueadaptivemusic.param.event.${paramName}.description", descriptions[paramName])
            }
        }
    }

    interface MusicEventCompanion: MusicTriggerCompanion {
        override fun getDisplayName(triggerName: String): Component {
            return Component.translatableWithFallback(
                "trueadaptivemusic.event.name.${triggerName}",
                TAMAPI.getEventType(triggerName)?.displayName ?: triggerName.prettify()
            )
        }

        override fun getArgDisplayName(triggerName: String, argName: String): Component? {
            val eventType = TAMAPI.getEventType(triggerName) ?: return null

            return translatableWithFallbackOrNull(
                "trueadaptivemusic.event.arg.${triggerName}.${argName}.display",
                eventType.argDisplayNames[argName]
            )
        }

        override fun getArgDescription(triggerName: String, argName: String): Component? {
            val eventType = TAMAPI.getEventType(triggerName) ?: return null

            return translatableWithFallbackOrNull(
                "trueadaptivemusic.event.arg.${triggerName}.${argName}.description",
                eventType.argDescriptions[argName]
            )
        }
    }
}