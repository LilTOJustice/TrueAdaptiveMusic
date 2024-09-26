package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.Constants
import net.minecraft.client.sound.AbstractSoundInstance
import net.minecraft.client.sound.AudioStream
import net.minecraft.client.sound.OggAudioStream
import net.minecraft.client.sound.SoundInstance
import net.minecraft.client.sound.SoundLoader
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Identifier
import java.io.File
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

class AdaptiveMusicSoundInstance(private val musicPath: Path): AbstractSoundInstance(
    Constants.TRUEADAPTIVEMUSIC_ID,
    SoundCategory.MUSIC,
    SoundInstance.createRandom()) {
    override fun getAudioStream (loader: SoundLoader, id: Identifier, repeatInstantly: Boolean):
            CompletableFuture<AudioStream> {
        return CompletableFuture.completedFuture(OggAudioStream(File(musicPath.toUri()).inputStream()))
    }
}
