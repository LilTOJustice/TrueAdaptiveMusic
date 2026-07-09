package liltojustice.trueadaptivemusic.common.network

import liltojustice.trueadaptivemusicapi.TrueAdaptiveMusicException

class TrueAdaptiveMusicNetworkingException(
    message: String? = null, inner: Exception? = null): TrueAdaptiveMusicException(message, inner)
