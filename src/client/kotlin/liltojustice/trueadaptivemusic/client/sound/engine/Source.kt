package liltojustice.trueadaptivemusic.client.sound.engine

import liltojustice.trueadaptivemusic.Logger
import net.minecraft.client.sound.AlUtil
import net.minecraft.client.sound.AudioStream
import net.minecraft.client.sound.StaticSound
import net.minecraft.util.math.Vec3d
import org.lwjgl.openal.AL10
import java.io.IOException
import javax.sound.sampled.AudioFormat

class Source private constructor(private val pointer: Int) {
    var playing: Boolean = true
    private var bufferSize = 16384
    private var stream: AudioStream? = null
    val isStopped: Boolean
        get() = this.sourceState == 4116
    var lastRead: Int? = null
        private set
    val sourceState: Int
        get() = if (!this.playing) 4116 else AL10.alGetSourcei(this.pointer, 4112)


    fun isPaused(): Boolean {
        return this.sourceState == 0x1013
    }

    fun setStereoRotation(rotationFromCenter: Float) {
        if (this.isStopped) {
            return
        }

        val angles = FloatArray(2)
        val rotationRadians = rotationFromCenter * PI / 180
        angles[0] = PI / 6.0f + rotationRadians
        angles[1] = -PI / 6.0f - rotationRadians
        AL10.alSourcefv(this.pointer, 0x1030, angles)
    }

    fun close() {
        if (!this.playing) {
            return
        }

        this.playing = false
        AL10.alSourceStop(this.pointer)
        AlUtil.checkErrors("Stop")
        this.stream?.let {
            try {
                it.close()
            } catch (e: IOException) {
                Logger.logError("Failed to close audio stream:\n${e.message}")
            }

            this.removeProcessedBuffers()
            this.stream = null
        }

        AL10.alDeleteSources(intArrayOf(this.pointer))
        AlUtil.checkErrors("Cleanup")

    }

    fun play() {
        AL10.alSourcePlay(this.pointer)
    }

    fun pause() {
        if (this.sourceState == 4114) {
            AL10.alSourcePause(this.pointer)
        }
    }

    fun resume() {
        if (this.sourceState == 4115) {
            AL10.alSourcePlay(this.pointer)
        }
    }

    fun stop() {
        if (this.playing) {
            AL10.alSourceStop(this.pointer)
            AlUtil.checkErrors("Stop")
        }
    }

    fun setPosition(pos: Vec3d) {
        AL10.alSourcefv(this.pointer, 4100, floatArrayOf(pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat()))
    }

    fun setVolume(volume: Float) {
        AL10.alSourcef(this.pointer, 4106, volume)
    }

    fun disableAttenuation() {
        AL10.alSourcei(this.pointer, 53248, 0)
    }

    fun setRelative(relative: Boolean) {
        AL10.alSourcei(this.pointer, 514, if (relative) 1 else 0)
    }

    fun setStream(stream: AudioStream) {
        this.stream = stream
        val audioFormat = stream.format
        this.bufferSize = getBufferSize(audioFormat)
        this.read(4)
    }

    private fun read(count: Int) {
        this.stream?.let { stream ->
            try {
                repeat(count) {
                    val byteBuffer = stream.read(this.bufferSize)
                    if (byteBuffer == null) {
                        this.lastRead = 0
                        return
                    }

                    StaticSound(byteBuffer, stream.format)
                        .takeStreamBufferPointer()
                        .ifPresent { pointer: Int ->
                            AL10.alSourceQueueBuffers(
                                this.pointer,
                                intArrayOf(pointer)
                            )
                        }
                }
            } catch (e: IOException) {
                Logger.logError("Failed to read from audio stream:\n${e.message}")
            }
        }
    }

    fun tick() {
        if (this.stream != null) {
            val i = this.removeProcessedBuffers()
            this.read(i)
        }
    }

    private fun removeProcessedBuffers(): Int {
        val finished = AL10.alGetSourcei(this.pointer, 4118)
        if (finished > 0) {
            val buffers = IntArray(finished)
            AL10.alSourceUnqueueBuffers(this.pointer, buffers)
            AlUtil.checkErrors("Unqueue buffers")
            AL10.alDeleteBuffers(buffers)
            AlUtil.checkErrors("Remove processed buffers")
        }

        return finished
    }

    companion object {
        const val PI = kotlin.math.PI.toFloat()

        fun create(): Source? {
            val i = IntArray(1)
            AL10.alGenSources(i)
            return if (AlUtil.checkErrors("Allocate new source")) null else Source(i[0])
        }

        private fun getBufferSize(format: AudioFormat,): Int {
            return (format.getSampleSizeInBits() / 8.0f * format.getChannels() * format.getSampleRate()).toInt()
        }
    }
}
