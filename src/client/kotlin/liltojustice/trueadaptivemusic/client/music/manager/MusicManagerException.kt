package liltojustice.trueadaptivemusic.client.music.manager

import liltojustice.trueadaptivemusic.TrueAdaptiveMusicException

class MusicManagerException(message: String? = null, inner: Exception? = null)
    : TrueAdaptiveMusicException(message, inner)