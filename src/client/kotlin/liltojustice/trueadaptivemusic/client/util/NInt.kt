package liltojustice.trueadaptivemusic.client.util

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kotlin.math.max

data class NInt(private var internal: UInt = 1U): Number(), Comparable<NInt> {
    init {
        internal = max(1U, internal)
    }

    override fun toDouble(): Double {
        return internal.toDouble()
    }

    override fun toFloat(): Float {
        return internal.toFloat()
    }

    override fun toLong(): Long {
        return internal.toLong()
    }

    override fun toInt(): Int {
        return internal.toInt()
    }

    override fun toShort(): Short {
        return internal.toShort()
    }

    override fun toByte(): Byte {
        return internal.toByte()
    }

    override fun toString(): String {
        return internal.toString()
    }

    override fun compareTo(other: NInt): Int {
        return internal.toInt() - other.internal.toInt()
    }

    fun toUInt(): UInt {
        return internal
    }

    object NIntTypeAdapter: TypeAdapter<NInt>() {
        override fun write(p0: JsonWriter, p1: NInt?) {
            p0.value(p1?.toInt() ?: 1)
        }

        override fun read(p0: JsonReader): NInt {
            return NInt(p0.nextInt().toUInt())
        }
    }
}
