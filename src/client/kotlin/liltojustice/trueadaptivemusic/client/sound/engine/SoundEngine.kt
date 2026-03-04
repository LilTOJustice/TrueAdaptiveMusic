package liltojustice.trueadaptivemusic.client.sound.engine

class SoundEngine {
    private val sources: MutableSet<Source> = mutableSetOf()

    fun release(source: Source) {
        synchronized(lock) {
            if (this.sources.remove(source)) {
                source.close()
            }
        }
    }

    fun close() {
        synchronized(lock) {
            this.sources.forEach { it.close() }
            this.sources.clear()
        }
    }

    fun createSource(): Source? {
        synchronized(lock) {
            val source = Source.create()
            source?.let { this.sources.add(it) }

            return source
        }
    }

    companion object {
        private val lock = Object()
    }
}