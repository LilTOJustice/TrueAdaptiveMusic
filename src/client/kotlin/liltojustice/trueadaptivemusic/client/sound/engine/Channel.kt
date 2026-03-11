package liltojustice.trueadaptivemusic.client.sound.engine

import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.sound.instance.TAMSoundInstance
import java.util.concurrent.locks.LockSupport
import java.util.function.Consumer

class Channel private constructor(
    private val soundEngine: SoundEngine,
    private val source: Source,
    private val soundInstance: TAMSoundInstance,
    private val startingVolume: Float,
) {
    private val thread = this.createThread()
    private val tasks = ArrayDeque<Consumer<Source>>()
    val isAmbient
        get() = soundInstance.isAmbient
    var isStopped: Boolean = false
        private set
    val almostDone: Boolean
        get() = source.lastRead == 0

    fun close() {
        stop()
        thread.interrupt()
        thread.join()
        tasks.clear()
    }

    fun run(action: Consumer<Source>) {
        if (isStopped) {
            return
        }

        tasks.add(action)
    }

    private fun stop() {
        if (isStopped) {
            return
        }
        isStopped = true
        soundEngine.release(source)
    }

    private fun createThread(): Thread {
        val thread = Thread {
            try {
                soundInstance.getAudioStream()?.use {
                    source.setVolume(startingVolume)
                    source.setStream(it)
                    source.setLooping(soundInstance.isLooping, soundInstance.loopStartPoint)
                    source.play()
                    waitForStop()
                }
            }
            catch (e: Exception) {
                if (e !is InterruptedException) {
                    Logger.logError("TAM Sound Engine thread encountered an exception: $e")
                    stop()
                }
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
                stop()
            }

            while (true) {
                val action = tasks.removeFirstOrNull() ?: break
                action.accept(source)
            }

            LockSupport.parkNanos("Sleeping for a bit", 1000000L)
        }
    }

    companion object {
        fun new(soundEngine: SoundEngine, soundInstance: TAMSoundInstance, startingVolume: Float): Channel? {
            val source = soundEngine.createSource() ?: return null

            return Channel(soundEngine, source, soundInstance, startingVolume,)
        }
    }
}
