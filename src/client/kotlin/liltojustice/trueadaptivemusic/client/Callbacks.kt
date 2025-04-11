package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.client.sound.PlayableSound

class Callbacks {
    companion object {
        fun getClientMusicManager(): MusicManager? {
            val result = Array<MusicManager?>(1) { null }
            GetMusicManagerCallback.EVENT.invoker().getMusicManager(result)
            return result[0]
        }

        fun getCurrentMusicPack(): MusicPack? {
            return getClientMusicManager()?.getMusicPack()
        }

        fun setCurrentMusicPack(musicPack: MusicPack?) {
            ChangeMusicPackCallback.EVENT.invoker().selectPack(musicPack)
            getClientMusicManager()?.selectMusicPack(musicPack)
        }

        fun refreshCurrentMusicPack() {
            setCurrentMusicPack(getCurrentMusicPack())
        }

        fun playSoundNow(sound: PlayableSound?, keepBackground: Boolean = false) {
            getClientMusicManager()?.playNow(sound, keepBackground)
        }

        fun invokeMusicEvent(eventName: String, vararg eventArgs: Any?) {
            InvokeMusicEventCallback.EVENT.invoker().invokeMusicEvent(eventName, *eventArgs)
        }

        fun eventActive(eventName: String): Boolean {
            return getClientMusicManager()?.hasActiveEvent(eventName) ?: false
        }
    }
}