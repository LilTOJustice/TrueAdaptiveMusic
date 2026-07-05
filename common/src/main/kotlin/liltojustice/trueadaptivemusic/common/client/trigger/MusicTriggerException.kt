package liltojustice.trueadaptivemusic.common.client.trigger

import liltojustice.trueadaptivemusic.common.TrueAdaptiveMusicException

class MusicTriggerException(message: String? = null, inner: Exception? = null)
    : TrueAdaptiveMusicException(message, inner)