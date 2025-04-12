package liltojustice.trueadaptivemusic.client.sound.instance

interface VolumeControlled {
    fun getVolume(): Float
    fun setVolume(volume: Float)
}
