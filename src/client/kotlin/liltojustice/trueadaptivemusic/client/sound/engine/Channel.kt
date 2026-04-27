package liltojustice.trueadaptivemusic.client.sound.engine

import liltojustice.trueadaptivemusic.client.sound.instance.TAMSoundInstance
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

    fun hasSecondsLeft(seconds: Float): Boolean {
        return source.hasSecondsLeft(seconds)
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
                    source.setLooping(soundInstance.isLoop, soundInstance.loopStartPoint)
                    source.play()
                    waitForStop()
                }
            }
            catch (e: Exception) {
                if (e !is InterruptedException) {
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
                tasks.removeFirstOrNull()?.accept(source) ?: break
            }

            Thread.sleep(1)
        }
    }

    companion object {
        fun new(soundEngine: SoundEngine, soundInstance: TAMSoundInstance, startingVolume: Float): Channel? {
            val source = soundEngine.createSource() ?: return null

            return Channel(soundEngine, source, soundInstance, startingVolume)
        }
    }
}
