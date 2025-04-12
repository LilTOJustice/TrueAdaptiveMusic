package liltojustice.trueadaptivemusic.client.sound

import liltojustice.trueadaptivemusic.client.sound.instance.VolumeControlled
import net.minecraft.client.option.SimpleOption
import net.minecraft.client.sound.SoundInstance
import net.minecraft.client.sound.SoundManager
import net.minecraft.client.sound.Source

fun SoundManager.setInstanceVolume(
    soundInstance: SoundInstance?, volume: Float, volumeOption: SimpleOption<Double>): Boolean {
    (soundInstance as? VolumeControlled)?.setVolume(volume)
    return runOnSource(soundInstance) { source -> source.setVolume(volume * volumeOption.value.toFloat()) }
}

fun SoundManager.resumeInstance(soundInstance: SoundInstance?): Boolean {
    return runOnSource(soundInstance, Source::resume)
}

fun SoundManager.pauseInstance(soundInstance: SoundInstance?): Boolean {
    return runOnSource(soundInstance, Source::pause)
}

private fun SoundManager.runOnSource(soundInstance: SoundInstance?, sourceConsumer: (source: Source) -> Unit): Boolean {
    return soundSystem.sources[soundInstance]?.run(sourceConsumer) == null
}
