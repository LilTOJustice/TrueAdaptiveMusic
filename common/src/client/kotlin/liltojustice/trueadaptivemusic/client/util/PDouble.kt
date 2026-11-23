package liltojustice.trueadaptivemusic.client.util

import kotlin.math.max

data class PDouble(private var internal: Double = 0.0): Number(), Comparable<PDouble> {
    init {
        internal = max(0.0, internal)
    }

    override fun toDouble(): Double {
        return internal
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
        return internal.toInt().toShort()
    }

    override fun toByte(): Byte {
        return internal.toInt().toByte()
    }

    override fun toString(): String {
        return internal.toString()
    }

    override fun compareTo(other: PDouble): Int {
        return internal.toInt() - other.internal.toInt()
    }
}
