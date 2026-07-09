package liltojustice.trueadaptivemusic.common.client.trigger

import liltojustice.trueadaptivemusicapi.TrueAdaptiveMusicException

class MusicTriggerException(message: String? = null, inner: Exception? = null)
    : TrueAdaptiveMusicException(message, inner)