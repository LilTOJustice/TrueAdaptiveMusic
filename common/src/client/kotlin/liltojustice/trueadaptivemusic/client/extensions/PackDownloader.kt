package liltojustice.trueadaptivemusic.client.extensions

import liltojustice.trueadaptivemusic.Reference
import liltojustice.trueadaptivemusic.client.browser.BrowsableMusicPack

typealias PackDownloader = suspend (musicPack: BrowsableMusicPack, progressOutput: Reference<Float>) -> Unit