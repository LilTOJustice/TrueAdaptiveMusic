package liltojustice.trueadaptivemusic.client.music.manager

import net.minecraft.client.sound.MusicTracker
import net.minecraft.client.sound.SoundInstance

fun MusicTracker.setCurrent(sound: SoundInstance?) {
    this.current = sound
}