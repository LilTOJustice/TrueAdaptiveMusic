package liltojustice.trueadaptivemusic

abstract class TrueAdaptiveMusicException(message: String?, inner: Exception? = null): Exception(message, inner) {
    override fun toString(): String {
        return if (cause != null)
            "${super.toString()}\nInner Exception: $cause"
        else
            super.toString()
    }
}