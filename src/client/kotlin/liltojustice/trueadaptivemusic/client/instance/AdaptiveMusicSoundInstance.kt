package liltojustice.trueadaptivemusic.client.instance

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.MusicLoadException
import liltojustice.trueadaptivemusic.client.sound.SoundFile
import net.minecraft.client.sound.AbstractSoundInstance
import net.minecraft.client.sound.AudioStream
import net.minecraft.client.sound.OggAudioStream
import net.minecraft.client.sound.SoundInstance
import net.minecraft.client.sound.SoundLoader
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture

class AdaptiveMusicSoundInstance(private val soundFile: SoundFile)
    : AbstractSoundInstance(Constants.TRUEADAPTIVEMUSIC_ID, SoundCategory.MUSIC, SoundInstance.createRandom()) {
    override fun getAudioStream (loader: SoundLoader, id: Identifier, repeatInstantly: Boolean):
            CompletableFuture<AudioStream> {
        return when(soundFile.getExtension()) {
            "ogg" -> CompletableFuture.completedFuture(OggAudioStream(soundFile.getInputStream()))
            else -> throw MusicLoadException("Invalid extension on file ${soundFile.getName()} (.${soundFile.getExtension()})")
        }
    }
}
