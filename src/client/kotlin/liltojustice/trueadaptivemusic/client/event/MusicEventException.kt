package liltojustice.trueadaptivemusic.client.event

import liltojustice.trueadaptivemusic.TrueAdaptiveMusicException

class MusicEventException(message: String? = null, inner: Exception? = null): TrueAdaptiveMusicException(message, inner)