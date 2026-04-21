package liltojustice.trueadaptivemusic.client.trigger.event

import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusicapi.trigger.event.arguments.EventArguments
import liltojustice.trueadaptivemusicapi.trigger.event.input.EventInput
import liltojustice.trueadaptivemusicapi.trigger.event.state.EventState
import liltojustice.trueadaptivemusicapi.trigger.event.type.EventType

class MusicEventFactory {
    fun fromArgs(
        eventType: EventType<*, *, *>,
        arguments: EventArguments,
        music: List<PlayableSound>,
        parameters: MusicEvent.Parameters
    ): MusicEvent<EventType<EventArguments, EventState, EventInput>, EventArguments, EventState, EventInput> {
        val eventType = eventType as EventType<EventArguments, EventState, EventInput>
        return MusicEvent(eventType, arguments, eventType.create(arguments), music, parameters)
    }

    fun makeCopy(event: MusicEvent<*, *, *, *>)
    : MusicEvent<EventType<EventArguments, EventState, EventInput>, EventArguments, EventState, EventInput> {
        val eventType = event.type as EventType<EventArguments, EventState, EventInput>
        return MusicEvent(eventType, event.arguments, event.state, event.music, event.parameters)
    }
}