package liltojustice.trueadaptivemusic.client.trigger

import liltojustice.trueadaptivemusic.TrueAdaptiveMusicException

class MusicTriggerException(message: String? = null, inner: Exception? = null)
    : TrueAdaptiveMusicException(message, inner)