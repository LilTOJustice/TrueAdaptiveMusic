package liltojustice.trueadaptivemusic.client.sound

import net.minecraft.client.sound.Source
import org.lwjgl.openal.AL10

fun Source.isPaused(): Boolean {
    return this.sourceState == 0x1013
}

fun Source.setStereoRotation(rotationFromCenter: Float) {
    if (this.isStopped) {
        return
    }

    val angles = FloatArray(2)
    val rotationRadians = rotationFromCenter * Constants.PI / 180
    angles[0] = Constants.PI / 6.0f + rotationRadians
    angles[1] = -Constants.PI / 6.0f - rotationRadians
    AL10.alSourcefv(this.pointer, 0x1030, angles)
}

private object Constants {
    const val PI = kotlin.math.PI.toFloat()
}