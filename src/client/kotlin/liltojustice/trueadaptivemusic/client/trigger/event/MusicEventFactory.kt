package liltojustice.trueadaptivemusic.client.trigger.event

import liltojustice.trueadaptivemusic.ReflectionHelper
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.trigger.MusicTriggerFactory

class MusicEventFactory(musicEventRegistry: MusicEventRegistry): MusicTriggerFactory<MusicEvent>(musicEventRegistry) {
    fun fromArgs(typeName: String, music: List<PlayableSound>, eventArgs: List<Any>, paramArgs: List<Any>): MusicEvent {
        val result = fromArgs(typeName, eventArgs)
        result.music = music
        result.parameters = MusicEvent.Parameters.fromArgs(paramArgs)

        return  result
    }

    fun makeCopy(musicEvent: MusicEvent): MusicEvent {
        return fromArgs(
            musicEvent.getTypeName(),
            musicEvent.music,
            ReflectionHelper.getConstructorParameterValues(musicEvent)
                .mapNotNull { it.value },
            ReflectionHelper.getConstructorParameterValues(musicEvent.parameters)
                .mapNotNull { it.value }
        )
    }
}