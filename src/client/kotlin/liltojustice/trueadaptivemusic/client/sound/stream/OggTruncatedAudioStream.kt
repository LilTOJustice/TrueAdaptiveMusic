package liltojustice.trueadaptivemusic.client.sound.stream

import net.minecraft.client.sound.OggAudioStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class OggTruncatedAudioStream(inputStream: InputStream): OggAudioStream(inputStream) {
    private var nonZeroRead: Boolean = true

    override fun getBuffer(size: Int): ByteBuffer {
        var numDiscarded = 0
        var resultArray: ByteArray?

        do {
            if (numDiscarded != 0) {
                println("Discarding buffer")
            }
            numDiscarded++
            resultArray = getTruncatedArray(size)
        } while (!nonZeroRead && resultArray != null)

        println("Discarded ${numDiscarded - 1} buffers")

        return resultArray?.let { makeByteBuffer(it) } ?: ByteBuffer.allocateDirect(0)
    }

    private fun getTruncatedArray(size: Int): ByteArray? {
        val buffer = super.getBuffer(size)
        val remaining = buffer.remaining()
        if (remaining == 0) {
            return null
        }

        val copyArray = ByteArray(remaining)
        buffer.get(copyArray)
        val resultArray = copyArray.dropWhile { it == zeroByte }.toByteArray()
        nonZeroRead = resultArray.isNotEmpty()

        return resultArray
    }

    companion object {
        private const val zeroByte = 0.toByte()

        private fun makeByteBuffer(bytes: ByteArray): ByteBuffer {
            val buffer = ByteBuffer.allocateDirect(bytes.size)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            buffer.put(bytes)
            return buffer.flip()
        }
    }
}