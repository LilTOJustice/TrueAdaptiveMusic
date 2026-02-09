package liltojustice.trueadaptivemusic.client.sound

import liltojustice.trueadaptivemusic.client.sound.instance.VolumeControlled
import net.minecraft.client.sound.SoundInstance
import net.minecraft.client.sound.SoundManager
import net.minecraft.client.sound.Source

fun SoundManager.setInstanceVolume(
    soundInstance: SoundInstance?, volume: Float, categoryVolume: Float): Boolean {
    (soundInstance as? VolumeControlled)?.setVolume(volume)

    return runOnSource(soundInstance) { source -> source.setVolume(volume * categoryVolume) }
}

fun SoundManager.resumeInstance(soundInstance: SoundInstance?): Boolean {
    return runOnSource(soundInstance, Source::resume)
}

fun SoundManager.pauseInstance(soundInstance: SoundInstance?): Boolean {
    return runOnSource(soundInstance, Source::pause)
}

fun SoundManager.isInstancePaused(soundInstance: SoundInstance?): Boolean {
    return getFromSource(soundInstance, Source::isPaused) ?: false
}

private fun SoundManager.runOnSource(soundInstance: SoundInstance?, sourceConsumer: (source: Source) -> Unit): Boolean {
    return soundSystem.sources[soundInstance]?.run(sourceConsumer) == null
}

private fun <T> SoundManager.getFromSource(soundInstance: SoundInstance?, sourceGetter: (source: Source) -> T): T? {
    var result: T? = null
    soundSystem.sources[soundInstance]?.run { result = (sourceGetter)(it) }

    return result
}