package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.TrueAdaptiveMusicException

class MusicLoadException(message: String?, inner: Exception? = null): TrueAdaptiveMusicException(message, inner)