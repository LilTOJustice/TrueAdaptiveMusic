package liltojustice.trueadaptivemusic.client.trigger.event

import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.event.type.EventTypeBase

class MusicEventFactory {
    fun fromArgs(
        eventType: EventTypeBase,
        arguments: TriggerArguments,
        music: List<PlayableSound>,
        parameters: MusicEvent.Parameters
    ): MusicEvent<*> {
        return MusicEvent(eventType, arguments, eventType.createStateBase(arguments), music, parameters)
    }

    fun makeCopy(existing: MusicEvent<*>): MusicEvent<*> {
        return MusicEvent(existing.type, existing.arguments, existing.state, existing.music, existing.parameters)
    }
}