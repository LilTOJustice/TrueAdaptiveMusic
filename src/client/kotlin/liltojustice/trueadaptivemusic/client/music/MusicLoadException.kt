package liltojustice.trueadaptivemusic.client.music

import liltojustice.trueadaptivemusic.TrueAdaptiveMusicException

class MusicLoadException(message: String? = null, inner: Exception? = null): TrueAdaptiveMusicException(message, inner)