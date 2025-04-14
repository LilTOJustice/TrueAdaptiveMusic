package liltojustice.trueadaptivemusic.client.sound.stream

import net.minecraft.client.sound.OggAudioStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class OggTruncatedAudioStream(inputStream: InputStream): OggAudioStream(inputStream) {
    private var nonZeroRead: Boolean = true
    private var isNew: Boolean = true

    override fun getBuffer(size: Int): ByteBuffer? {
        var numDiscarded = 0
        var resultArray: ByteArray?

        do {
            if (numDiscarded != 0) {
                println("Discarding buffer")
            }
            numDiscarded++
            resultArray = getTruncatedArray(size)
            isNew = false
        } while (!nonZeroRead && resultArray != null)

        return resultArray?.let { makeByteBuffer(it) }
    }

    private fun getTruncatedArray(size: Int): ByteArray? {
        val buffer = super.getBuffer(size)
        val remaining = buffer.remaining()
        if (remaining == 0) {
            return null
        }

        val copyArray = ByteArray(remaining)
        buffer.get(copyArray)
        val bytesPerSample = format.sampleSizeInBits / 8
        val resultArray = copyArray
            .toList()
            .chunked(bytesPerSample)
            .dropWhile {
                val first = it.first()
                it.all { byte -> byte == first }
            }
            .flatten()
            .let {
                val padding = it.size % 4
                it.plus(List(padding) { 0 })
            }
            .toByteArray()

        nonZeroRead = resultArray.isNotEmpty()

        return if (nonZeroRead) copyArray else resultArray
    }

    companion object {
        private fun makeByteBuffer(bytes: ByteArray): ByteBuffer {
            val buffer = ByteBuffer.allocateDirect(bytes.size)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            buffer.put(bytes)
            return buffer.flip()
        }
    }
}