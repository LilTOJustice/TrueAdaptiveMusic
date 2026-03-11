package liltojustice.trueadaptivemusic.client.sound.engine

import liltojustice.trueadaptivemusic.Logger
import net.minecraft.client.sound.AlUtil
import net.minecraft.client.sound.AudioStream
import net.minecraft.client.sound.StaticSound
import org.lwjgl.openal.AL10
import org.lwjgl.openal.AL11
import java.io.IOException
import javax.sound.sampled.AudioFormat
import kotlin.math.PI

class Source private constructor(private val pointer: Int) {
    var playing: Boolean = true
    private var bufferSize = 0
    private var stream: AudioStream? = null
    private var loopStartPointSeconds = 0F
    private var lastTimestamp = 0F
    val isStopped: Boolean
        get() = this.sourceState == AL_STOPPED
    var lastRead: Int? = null
        private set
    val sourceState: Int
        get() = if (!this.playing) AL_STOPPED else AL10.alGetSourcei(this.pointer, AL_SOURCE_STATE)

    init {
        AL10.alSourcei(this.pointer, AL_SOURCE_RELATIVE, 1)
        AL10.alSourcei(this.pointer, AL_DISTANCE_MODEL, 0)
    }

    fun isPaused(): Boolean {
        return this.sourceState == AL_PAUSED
    }

    fun setStereoRotation(rotationFromCenter: Float) {
        if (this.isStopped) {
            return
        }

        val angles = FloatArray(2)
        val rotationRadians = rotationFromCenter * FPI / 180
        angles[0] = FPI / 6.0f + rotationRadians
        angles[1] = -FPI / 6.0f - rotationRadians
        AL10.alSourcefv(this.pointer, AL_STEREO_ANGLES, angles)
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
        if (this.sourceState == AL_PLAYING) {
            AL10.alSourcePause(this.pointer)
        }
    }

    fun resume() {
        if (this.sourceState == AL_PAUSED) {
            AL10.alSourcePlay(this.pointer)
        }
    }

    fun stop() {
        if (this.playing) {
            AL10.alSourceStop(this.pointer)
            AlUtil.checkErrors("Stop")
        }
    }

    fun setVolume(volume: Float) {
        AL10.alSourcef(this.pointer, AL_GAIN, volume)
    }

    fun setStream(stream: AudioStream) {
        this.stream = stream
        val audioFormat = stream.format
        this.bufferSize = getBufferSize(audioFormat)
        this.read()
    }

    fun setLooping(looping: Boolean, loopStartPoint: UInt) {
        this.loopStartPointSeconds = loopStartPoint.toFloat() / 1000F
        AL10.alSourcei(this.pointer, AL_LOOPING, if (looping) 1 else 0)
    }

    private fun read() {
        this.stream?.let { stream ->
            try {
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
            } catch (e: IOException) {
                Logger.logError("Failed to read from audio stream:\n${e.message}")
            }
        }
    }

    fun tick() {
        if (this.stream != null && this.playing) {
            val newTimestamp = AL11.alGetSourcef(this.pointer, AL_SEC_OFFSET)
            if (newTimestamp < lastTimestamp) {
                AL11.alSourcef(this.pointer, AL_SEC_OFFSET, loopStartPointSeconds)
                AlUtil.checkErrors("seek")
            }

            lastTimestamp = newTimestamp

            this.read()
        }
    }

    private fun removeProcessedBuffers() {
        val finished = AL10.alGetSourcei(this.pointer, AL_BUFFERS_PROCESSED)
        if (finished > 0) {
            val buffers = IntArray(finished)
            AL10.alSourceUnqueueBuffers(this.pointer, buffers)
            AlUtil.checkErrors("Unqueue buffers")
            AL10.alDeleteBuffers(buffers)
            AlUtil.checkErrors("Remove processed buffers")
        }
    }

    companion object {
        private const val FPI = PI.toFloat()
        private const val BYTE = 8.0F
        private const val AL_SOURCE_RELATIVE = 514
        private const val AL_BUFFERS_PROCESSED = 4118
        private const val AL_SEC_OFFSET = 4132
        private const val AL_LOOPING = 4103
        private const val AL_GAIN = 4106
        private const val AL_SOURCE_STATE = 4112
        private const val AL_PLAYING = 4114
        private const val AL_PAUSED = 4115
        private const val AL_STOPPED = 4116
        private const val AL_STEREO_ANGLES = 4144
        private const val AL_DISTANCE_MODEL = 53248

        fun create(): Source? {
            val i = IntArray(1)
            AL10.alGenSources(i)
            return if (AlUtil.checkErrors("Allocate new source")) null else Source(i[0])
        }

        private fun getBufferSize(format: AudioFormat): Int {
            return (format.getSampleSizeInBits() / BYTE * format.getChannels() * format.getSampleRate()).toInt()
        }
    }
}
