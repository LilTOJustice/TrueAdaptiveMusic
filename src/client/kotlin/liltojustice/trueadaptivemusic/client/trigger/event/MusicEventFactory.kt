package liltojustice.trueadaptivemusic.client.trigger.event

import liltojustice.trueadaptivemusic.ReflectionHelper
import liltojustice.trueadaptivemusic.client.trigger.MusicTriggerFactory

class MusicEventFactory(musicEventRegistry: MusicEventRegistry)
    : MusicTriggerFactory<MusicEvent, MusicEvent.Parameters>(
    musicEventRegistry, { json, e -> ErrorEvent(json, e.message ?: "Unknown") }) {

    fun makeCopy(musicEvent: MusicEvent): MusicEvent {
        return fromArgs(
            musicEvent.getTypeName(),
            musicEvent.music,
            ReflectionHelper.getConstructorParameterValues(musicEvent.parameters)
                .mapNotNull { it.value },
            ReflectionHelper.getConstructorParameterValues(musicEvent)
                .mapNotNull { it.value }
        )
    }
}