package liltojustice.trueadaptivemusic.client.sound.system

import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.sound.instance.TAMSoundInstance
import liltojustice.trueadaptivemusic.client.sound.isPaused
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.option.GameOptions
import net.minecraft.client.sound.Channel
import net.minecraft.client.sound.Channel.SourceManager
import net.minecraft.client.sound.SoundEngine
import net.minecraft.client.sound.SoundExecutor
import net.minecraft.client.sound.Source
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Util
import java.util.concurrent.atomic.AtomicReference

@Environment(EnvType.CLIENT)
class SoundSystem(private val options: GameOptions) {
    private var started = false
    private val soundEngine = SoundEngine()
    private var lastSoundDeviceCheckTime: Long = 0
    private val deviceChangeStatus = AtomicReference(DeviceChangeStatus.NO_CHANGE)
    val sources = mutableMapOf<TAMSoundInstance, SourceContext>()

    fun stop() {
        if (!started) {
            return
        }

        stopAll()
        soundEngine.close()
        started = false
    }

    fun stop(soundInstance: TAMSoundInstance?) {
        if (!started) {
            return
        }

        sources[soundInstance]?.manager?.run(Source::stop)
    }

    fun stopAll() {
        if (!started) {
            return
        }

        sources.values.forEach { context ->
            context.soundExecutor.stop()
            context.channel.close()
        }
        sources.clear()
    }

    private fun shouldRestart(): Boolean {
        if (this.soundEngine.isDeviceUnavailable) {
            Logger.logWarning("Audio device was lost!")
            return true
        }
        else {
            val now = Util.getMeasuringTimeMs()
            if (now - lastSoundDeviceCheckTime >= 1000L) {
                lastSoundDeviceCheckTime = now
                if (deviceChangeStatus.compareAndSet(
                        DeviceChangeStatus.NO_CHANGE,
                        DeviceChangeStatus.ONGOING)) {
                    val soundDeviceName = options.soundDevice.getValue()
                    Util.getIoWorkerExecutor().execute {
                        if (soundDeviceName.isEmpty()) {
                            if (soundEngine.updateDeviceSpecifier()) {
                                Logger.logInfo("System default audio device has changed!")
                                deviceChangeStatus.compareAndSet(
                                    DeviceChangeStatus.ONGOING,
                                    DeviceChangeStatus.CHANGE_DETECTED)
                            }
                        } else if (
                            soundEngine.getCurrentDeviceName() != soundDeviceName &&
                            soundEngine.getSoundDevices().contains(soundDeviceName)) {
                            Logger.logInfo("Preferred audio device has become available!")
                            deviceChangeStatus.compareAndSet(
                                DeviceChangeStatus.ONGOING,
                                DeviceChangeStatus.CHANGE_DETECTED)
                        }
                        deviceChangeStatus.compareAndSet(
                            DeviceChangeStatus.ONGOING,
                            DeviceChangeStatus.NO_CHANGE)
                    }
                }
            }

            return deviceChangeStatus.compareAndSet(
                DeviceChangeStatus.CHANGE_DETECTED,
                DeviceChangeStatus.NO_CHANGE)
        }
    }

    fun tick() {
        if (!started) {
            start()
        }

        if (shouldRestart()) {
            stop()
            start()
        }

        sources
            .filter { it.value.manager.isStopped }
            .forEach {
                it.value.soundExecutor.stop()
                it.value.channel.close()
                sources.remove(it.key)
            }
        sources.values.forEach {
            it.channel.tick()
        }
    }

    fun isPlaying(soundInstance: TAMSoundInstance?): Boolean {
        return !(sources[soundInstance]?.manager?.isStopped ?: true)
    }

    fun play(soundInstance: TAMSoundInstance) {
        if (!started) {
            return
        }

        val volume = soundInstance.desiredVolume
        val sourceContext = SourceContext.new(soundEngine) ?: return
        sources[soundInstance] = sourceContext
        sourceContext.manager.run { source: Source ->
            soundInstance.getAudioStream()?.let {
                source.setVolume(volume)
                source.setStream(it)
                source.play()
            }
        }
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
        return sources[soundInstance]?.manager?.run(sourceConsumer) == null
    }

    private fun <T> getFromSource(soundInstance: TAMSoundInstance?, sourceGetter: (source: Source) -> T): T? {
        var result: T? = null
        sources[soundInstance]?.manager?.run { result = (sourceGetter)(it) }

        return result
    }

    private fun start() {
        if (started) {
            return
        }

        try {
            val soundDeviceName = options.soundDevice.getValue()
            soundEngine.init(
                if ("" == soundDeviceName)
                    null
                else soundDeviceName,
                options.directionalAudio.getValue())
            started = true
        } catch (e: RuntimeException) {
            Logger.logError("Error starting TAM sound system:\n$e")
        }
    }

    private fun getProperSourceVolume(soundInstance: TAMSoundInstance): Float {
        return soundInstance.desiredVolume * options.getSoundVolume(SoundCategory.MASTER) * options.getSoundVolume(
            if (soundInstance.isAmbient) {
                SoundCategory.AMBIENT
            }
            else {
                SoundCategory.MUSIC
            }
        )
    }

    @Environment(EnvType.CLIENT)
    enum class DeviceChangeStatus {
        ONGOING,
        CHANGE_DETECTED,
        NO_CHANGE
    }

    @ConsistentCopyVisibility
    data class SourceContext private constructor(
        val soundExecutor: SoundExecutor, val channel: Channel, val manager: SourceManager) {
        companion object {
            fun new(soundEngine: SoundEngine, ): SourceContext? {
                val executor = SoundExecutor()
                val channel = Channel(soundEngine, executor)
                val source = channel.createSource(SoundEngine.RunMode.STREAMING).join() ?: return null
                return SourceContext(executor, channel, source)
            }
        }
    }
}
