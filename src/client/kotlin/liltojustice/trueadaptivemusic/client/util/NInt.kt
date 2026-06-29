package liltojustice.trueadaptivemusic.client.util

data class NInt(private val internal: UInt = 1U): Number() {
    init {
        require(internal > 0U)
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
}

fun String.toNIntOrNull(): NInt? = toUIntOrNull(radix = 10)?.let { NInt(it) }
