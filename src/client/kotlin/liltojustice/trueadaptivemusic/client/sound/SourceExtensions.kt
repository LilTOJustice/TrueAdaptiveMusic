package liltojustice.trueadaptivemusic.client.sound

import net.minecraft.client.sound.Source

fun Source.isPaused(): Boolean {
    return this.sourceState == 0x1013
}