package liltojustice.trueadaptivemusic.client.sound.engine

import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.sound.instance.TAMSoundInstance
import liltojustice.trueadaptivemusic.client.sound.isPaused
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.option.GameOptions
import net.minecraft.client.sound.SoundEngine
import net.minecraft.client.sound.Source
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Util
import java.util.concurrent.atomic.AtomicReference
import kotlin.collections.get

@Environment(EnvType.CLIENT)
class SoundSystem(private val options: GameOptions) {
    private var started = false
    private val soundEngine = SoundEngine()
    private var lastSoundDeviceCheckTime: Long = 0
    private val deviceChangeStatus = AtomicReference(DeviceChangeStatus.NO_CHANGE)
    val sources = mutableMapOf<TAMSoundInstance, Channel>()

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

        sources[soundInstance]?.run(Source::stop)
    }

    fun stopAll() {
        if (!started) {
            return
        }

        sources.values.forEach { it.close() }
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
                        }
                        else if (soundEngine.getCurrentDeviceName() != soundDeviceName &&
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
        if (!started) {
            return
        }

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
}