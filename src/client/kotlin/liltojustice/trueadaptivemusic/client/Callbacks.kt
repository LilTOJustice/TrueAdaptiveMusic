package liltojustice.trueadaptivemusic.client

class Callbacks {
    companion object {
        fun getCurrentMusicPack(): MusicPack? {
            val packResult = Array<MusicPack?>(1) { null }
            GetMusicPackCallback.EVENT.invoker().getPack(packResult)
            return packResult[0]
        }

        fun setCurrentMusicPack(musicPack: MusicPack?) {
            ChangeMusicPackCallback.EVENT.invoker().selectPack(musicPack)
        }

        fun refreshCurrentMusicPack() {
            setCurrentMusicPack(getCurrentMusicPack())
        }
    }
}