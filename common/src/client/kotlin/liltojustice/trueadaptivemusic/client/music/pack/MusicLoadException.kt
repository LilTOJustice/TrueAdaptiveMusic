package liltojustice.trueadaptivemusic.client.music.pack

import liltojustice.trueadaptivemusicapi.TrueAdaptiveMusicException

class MusicLoadException(message: String? = null, inner: Exception? = null): TrueAdaptiveMusicException(message, inner)