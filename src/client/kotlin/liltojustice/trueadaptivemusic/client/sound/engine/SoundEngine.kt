package liltojustice.trueadaptivemusic.client.sound.engine

import com.google.common.collect.Sets
import net.minecraft.client.sound.Source
import java.util.function.Consumer

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
        private val sources: MutableSet<Source?> = Sets.newIdentityHashSet<Source?>()

        fun createSource(): Source? {
            val source = Source.create()
            if (source != null) {
                this.sources.add(source)
            }

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
            this.sources.forEach(Consumer { obj: Source? -> obj!!.close() })
            this.sources.clear()
        }
    }
}