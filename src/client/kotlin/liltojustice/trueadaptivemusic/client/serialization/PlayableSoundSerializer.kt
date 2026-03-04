package liltojustice.trueadaptivemusic.client.serialization

import com.google.gson.JsonObject
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.sound.SoundLibrary
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.trigger.MusicTriggerException

object PlayableSoundSerializer {
    class PlayableSoundTypeAdapter(private val soundLibrary: SoundLibrary?): TypeAdapter<PlayableSound>() {
        override fun write(output: JsonWriter, sound: PlayableSound) {
            output.value(sound.getSoundName())
        }

        override fun read(input: JsonReader): PlayableSound? {
            val path = input.nextString()
            val library = soundLibrary
                ?: throw MusicTriggerException(
                    "No sound library given for deserializing sound files from trigger.")
            return PlayableSound.Companion.of(path, library)
                ?: run {
                    Logger.logWarning("Could not find sound for \"$path\", skipping...")
                    null
                }
        }
    }
}