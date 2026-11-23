package liltojustice.trueadaptivemusic.client.extensions

import liltojustice.trueadaptivemusic.client.browser.PackManifest

typealias PackFetcher = suspend (ignoreCache: Boolean) -> PackManifest?