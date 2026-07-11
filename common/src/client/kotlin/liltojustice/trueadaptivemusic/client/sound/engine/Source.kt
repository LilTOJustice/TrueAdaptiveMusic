package liltojustice.trueadaptivemusic.client.sound.engine

import com.mojang.blaze3d.audio.OpenAlUtil
import com.mojang.blaze3d.audio.SoundBuffer
import liltojustice.trueadaptivemusic.Logger
import net.minecraft.client.sounds.AudioStream
import org.lwjgl.openal.AL10
import org.lwjgl.openal.AL11
import java.io.IOException
import javax.sound.sampled.AudioFormat
import kotlin.math.PI

class Source private constructor(private val pointer: Int) {
    var playing = true
    private var bufferSize = 0
    private var stream: AudioStream? = null
    private var looping = false
    private var loopStartPointSeconds = 0F
    private var lastTimestamp = 0F
    private var totalSeconds: Float? = null
    private var totalBytes = 0UL
    val isStopped: Boolean
        get() = this.sourceState == AL_STOPPED
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
        OpenAlUtil.checkALError("Stop")
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
        OpenAlUtil.checkALError("Cleanup")
    }

    fun play() {
        AL10.alSourcePlay(this.pointer)
        OpenAlUtil.checkALError("Play")
    }

    fun pause() {
        if (this.sourceState == AL_PLAYING) {
            AL10.alSourcePause(this.pointer)
            OpenAlUtil.checkALError("Pause")
        }
    }

    fun resume() {
        if (this.sourceState == AL_PAUSED) {
            AL10.alSourcePlay(this.pointer)
            OpenAlUtil.checkALError("Resume")
        }
    }

    fun stop() {
        if (this.playing) {
            AL10.alSourceStop(this.pointer)
            OpenAlUtil.checkALError("Stop")
        }
    }

    fun setVolume(volume: Float) {
        AL10.alSourcef(this.pointer, AL_GAIN, volume)
        OpenAlUtil.checkALError("Set Volume")
    }

    fun setStream(stream: AudioStream) {
        this.stream = stream
        val audioFormat = stream.format
        this.bufferSize = getBufferSize(audioFormat)
        this.read()

        if (!looping) {
            repeat(3) { this.read() }
        }
    }

    fun setLooping(looping: Boolean, loopStartPoint: UInt) {
        this.loopStartPointSeconds = loopStartPoint.toFloat() / 1000F
        this.looping = looping
        AL10.alSourcei(this.pointer, AL_LOOPING, if (looping) 1 else 0)
        OpenAlUtil.checkALError("Set Looping")
    }

    fun tick() {
        if (this.stream == null || !this.playing) {
            return
        }
        else if (looping) {
            tickLooping()
        }
        else {
            repeat(removeProcessedBuffers()) { read() }
        }
    }

    fun hasSecondsLeft(seconds: Float): Boolean {
        return totalSeconds?.let { it - lastTimestamp < seconds } ?: false
    }

    private fun read(): Boolean {
        this.stream?.let { stream ->
            try {
                val byteBuffer = stream.read(this.bufferSize)
                if (byteBuffer.remaining() == 0) {
                    return false
                }

                totalBytes += byteBuffer.remaining().toULong()

                SoundBuffer(byteBuffer, stream.format)
                    .releaseAlBuffer()
                    .ifPresent { pointer: Int ->
                        AL10.alSourceQueueBuffers(
                            this.pointer,
                            intArrayOf(pointer)
                        )
                    }
            } catch (e: IOException) {
                Logger.logError("Failed to read from audio stream:\n${e.message}")
            }

            return true
        }

        return true
    }

    private fun tickLooping() {
        val newTimestamp = AL11.alGetSourcef(this.pointer, AL_SEC_OFFSET)
        if (newTimestamp < lastTimestamp) {
            AL11.alSourcef(this.pointer, AL_SEC_OFFSET, loopStartPointSeconds)
            OpenAlUtil.checkALError("Seek")
        }

        lastTimestamp = newTimestamp

        if (!read() && totalSeconds == null) {
            totalSeconds = (totalBytes.toFloat() / bufferSize)
        }

    }

    private fun removeProcessedBuffers(): Int {
        val finished = AL10.alGetSourcei(this.pointer, AL_BUFFERS_PROCESSED)
        if (finished > 0) {
            val buffers = IntArray(finished)
            AL10.alSourceUnqueueBuffers(this.pointer, buffers)
            OpenAlUtil.checkALError("Unqueue buffers")
            AL10.alDeleteBuffers(buffers)
            OpenAlUtil.checkALError("Remove processed buffers")
        }

        return finished
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
            return if (OpenAlUtil.checkALError("Allocate new source")) null else Source(i[0])
        }

        private fun getBufferSize(format: AudioFormat): Int {
            return (format.getSampleSizeInBits() / BYTE * format.getChannels() * format.getSampleRate()).toInt()
        }
    }
}
