package liltojustice.trueadaptivemusic.client.sound.engine

import liltojustice.trueadaptivemusic.client.sound.instance.TAMSoundInstance
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.option.GameOptions
import net.minecraft.sound.SoundCategory
import kotlin.collections.get

@Environment(EnvType.CLIENT)
class SoundSystem(private val options: GameOptions) {
    private val soundEngine = SoundEngine()
    val channels = mutableMapOf<TAMSoundInstance, Channel>()

    fun stop(soundInstance: TAMSoundInstance?) {
        channels[soundInstance]?.run(Source::stop)
    }

    fun stopAll() {
        soundEngine.close()
        channels.values.forEach { it.close() }
        channels.clear()
    }

    fun tick(yaw: Float? = null) {
        channels.filter { it.value.isStopped }.forEach { channels.remove(it.key) }
        yaw?.let {
            channels.values
                .filter { channel -> channel.isAmbient }
                .forEach { channel -> channel.run { source -> source.setStereoRotation(yaw) } }
        }
    }

    fun isPlaying(soundInstance: TAMSoundInstance?): Boolean {
        return !(channels[soundInstance]?.isStopped ?: true)
    }

    fun isAlmostDone(soundInstance: TAMSoundInstance?): Boolean {
        return channels[soundInstance]?.almostDone ?: false
    }

    fun play(soundInstance: TAMSoundInstance) {
        channels[soundInstance] = Channel.new(
            soundEngine, soundInstance, getProperSourceVolume(soundInstance)) ?: return
    }

    fun refreshSoundVolume() {
        channels.keys.forEach { refreshSoundVolume(it) }
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
        return channels[soundInstance]?.run(sourceConsumer) == null
    }

    private fun <T> getFromSource(soundInstance: TAMSoundInstance?, sourceGetter: (source: Source) -> T): T? {
        var result: T? = null
        channels[soundInstance]?.run { result = (sourceGetter)(it) }

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