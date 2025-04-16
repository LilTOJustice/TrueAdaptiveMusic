package liltojustice.trueadaptivemusic

open class TrueAdaptiveMusicException(message: String? = null, inner: Exception? = null): Exception(message, inner) {
    override fun toString(): String {
        return cause?.let {
            "${super.toString()}\nInner Exception: $it" +
                    if (it !is TrueAdaptiveMusicException)
                        "\nStack Trace:${it.stackTraceToString()}"
                    else
                        ""
        } ?: "${super.toString()}\nStack Trace:${stackTraceToString()}"
    }

    private fun stackTraceToString(): String {
        val result = StringBuilder()
        stackTrace.forEach { element -> result.append("\n${element.toString()}") }

        return result.toString()
    }
}