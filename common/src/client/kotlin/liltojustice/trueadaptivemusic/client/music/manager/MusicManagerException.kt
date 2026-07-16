package liltojustice.trueadaptivemusic.client.music.manager

import liltojustice.trueadaptivemusicapi.TrueAdaptiveMusicException

class MusicManagerException(message: String? = null, inner: Exception? = null)
    : TrueAdaptiveMusicException(message, inner)