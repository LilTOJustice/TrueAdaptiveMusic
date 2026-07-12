package liltojustice.trueadaptivemusic.client.trigger

import liltojustice.trueadaptivemusicapi.TrueAdaptiveMusicException

class MusicTriggerException(message: String? = null, inner: Exception? = null)
    : TrueAdaptiveMusicException(message, inner)