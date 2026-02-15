package liltojustice.trueadaptivemusic.client.sound.engine

import liltojustice.trueadaptivemusic.client.sound.instance.TAMSoundInstance
import net.minecraft.client.sound.SoundEngine
import net.minecraft.client.sound.Source
import java.util.concurrent.locks.LockSupport
import java.util.function.Consumer

class Channel private constructor(
    private val soundEngine: SoundEngine,
    private val source: Source,
    private val soundInstance: TAMSoundInstance,
    private val startingVolume: Float
) {
    private val thread = this.createThread()
    private val tasks = ArrayDeque<Consumer<Source>>()
    var isStopped: Boolean = false
        private set

    fun close() {
        if (isStopped) {
            return
        }

        isStopped = true
        soundEngine.release(source)
        source.close()
        thread.interrupt()

        try {
            thread.join()
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    fun run(action: Consumer<Source>) {
        if (isStopped) {
            return
        }

        tasks.add(action)
    }

    private fun createThread(): Thread {
        val thread = Thread {
            try {
                soundInstance.getAudioStream()?.let {
                    source.setVolume(startingVolume)
                    source.setStream(it)
                    source.play()
                }
                waitForStop()
            }
            catch (_: Exception) {
                close()
            }
        }
        thread.setDaemon(true)
        thread.setName("TAM Sound Engine: ${soundInstance.hashCode()}")
        thread.start()
        return thread
    }

    private fun waitForStop() {
        while (!isStopped) {
            source.tick()
            if (source.isStopped) {
                close()
            }

            while (true) {
                val action = tasks.removeFirstOrNull() ?: break
                action.accept(source)
            }

            LockSupport.parkNanos("Sleeping for a bit", 100000L)
        }
    }

    companion object {
        fun new(soundEngine: SoundEngine, soundInstance: TAMSoundInstance, startingVolume: Float): Channel? {
            val source = soundEngine.createSource(SoundEngine.RunMode.STREAMING) ?: return null

            return Channel(soundEngine, source, soundInstance, startingVolume)
        }
    }
}
