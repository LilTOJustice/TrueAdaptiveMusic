package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.client.sound.PlayableSound

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

        fun playSoundNow(sound: PlayableSound?) {
            PlaySoundNowCallback.EVENT.invoker().playSoundNow(sound)
        }
    }
}