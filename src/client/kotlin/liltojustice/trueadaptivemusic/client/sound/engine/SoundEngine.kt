package liltojustice.trueadaptivemusic.client.sound.engine

import net.minecraft.client.sound.Source

class SoundEngine {
    private var sources = SourceSet()

    fun createSource(): Source? {
        return this.sources.createSource()
    }

    fun release(source: Source) {
        check(this.sources.release(source)) { "Tried to release unknown channel" }
    }

    fun close() {
        this.sources.close()
    }

    class SourceSet() {
        private val sources: MutableSet<Source> = mutableSetOf()

        fun createSource(): Source? {
            val source = Source.create()
            source?.let { this.sources.add(it) }

            return source
        }

        fun release(source: Source): Boolean {
            if (!this.sources.remove(source)) {
                return false
            } else {
                source.close()
                return true
            }
        }

        fun close() {
            this.sources.forEach { it.close() }
            this.sources.clear()
        }
    }
}