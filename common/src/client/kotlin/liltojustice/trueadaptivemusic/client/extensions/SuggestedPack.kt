package liltojustice.trueadaptivemusic.client.extensions

import liltojustice.trueadaptivemusic.client.browser.BrowsableMusicPack

data class SuggestedPack(val name: String,  val version: String) {
    override fun equals(other: Any?): Boolean {
        if (other is BrowsableMusicPack) {
            return name == other.name && version == (other.version ?: "")
        }

        return super.equals(other)
    }
}