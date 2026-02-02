package liltojustice.trueadaptivemusic.client.sound

import net.minecraft.client.sound.Source

fun Source.isPaused(): Boolean {
    return this.getSourceState() == 0x1013
}