package liltojustice.trueadaptivemusic.client.sound.engine

import liltojustice.trueadaptivemusic.client.sound.instance.TAMSoundInstance
import liltojustice.trueadaptivemusic.client.sound.isPaused
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.option.GameOptions
import net.minecraft.client.sound.Source
import net.minecraft.sound.SoundCategory
import kotlin.collections.get

@Environment(EnvType.CLIENT)
class SoundSystem(private val options: GameOptions) {
    private val soundEngine = SoundEngine()
    val sources = mutableMapOf<TAMSoundInstance, Channel>()

    fun stop(soundInstance: TAMSoundInstance?) {
        sources[soundInstance]?.run(Source::stop)
    }

    fun stopAll() {
        soundEngine.close()
        sources.clear()
    }

    fun tick() {
        sources
            .filter { it.value.isStopped }
            .forEach {
                it.value.close()
                sources.remove(it.key)
            }
    }

    fun isPlaying(soundInstance: TAMSoundInstance?): Boolean {
        return !(sources[soundInstance]?.isStopped ?: true)
    }

    fun play(soundInstance: TAMSoundInstance) {
        sources[soundInstance] = Channel.new(
            soundEngine, soundInstance, getProperSourceVolume(soundInstance)) ?: return
    }

    fun refreshSoundVolume() {
        sources.keys.forEach { refreshSoundVolume(it) }
    }

    fun refreshSoundVolume(soundInstance: TAMSoundInstance) {
        runOnSource(soundInstance) { source -> source.setVolume(getProperSourceVolume(soundInstance)) }
    }

    fun setInstanceVolume(soundInstance: TAMSoundInstance, volume: Float): Boolean {
        soundInstance.desiredVolume = volume

        return runOnSource(soundInstance) { source -> source.setVolume(getProperSourceVolume(soundInstance)) }
    }

    fun resumeInstance(soundInstance: TAMSoundInstance?): Boolean {
        return runOnSource(soundInstance, Source::resume)
    }

    fun pauseInstance(soundInstance: TAMSoundInstance?): Boolean {
        return runOnSource(soundInstance, Source::pause)
    }

    fun isInstancePaused(soundInstance: TAMSoundInstance?): Boolean {
        return getFromSource(soundInstance, Source::isPaused) ?: false
    }

    private fun runOnSource(soundInstance: TAMSoundInstance?, sourceConsumer: (source: Source) -> Unit): Boolean {
        return sources[soundInstance]?.run(sourceConsumer) == null
    }

    private fun <T> getFromSource(soundInstance: TAMSoundInstance?, sourceGetter: (source: Source) -> T): T? {
        var result: T? = null
        sources[soundInstance]?.run { result = (sourceGetter)(it) }

        return result
    }

    private fun getProperSourceVolume(soundInstance: TAMSoundInstance): Float {
        return soundInstance.desiredVolume * options.getSoundVolume(SoundCategory.MASTER) *
                options.getSoundVolume(
                    if (soundInstance.isAmbient) {
                        SoundCategory.AMBIENT
                    }
                    else {
                        SoundCategory.MUSIC
                    }
                )
    }
}