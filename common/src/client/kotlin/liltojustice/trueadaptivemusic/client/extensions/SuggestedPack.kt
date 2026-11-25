package liltojustice.trueadaptivemusic.client.extensions

import liltojustice.trueadaptivemusic.client.browser.BrowsableMusicPack
import kotlin.io.path.name

data class SuggestedPack(val name: String) {
    override fun equals(other: Any?): Boolean {
        if (other is BrowsableMusicPack) {
            return name == other.getFilePath().name
        }

        return super.equals(other)
    }
}