package liltojustice.trueadaptivemusic.client.sound.file

import java.io.InputStream
import javax.sound.sampled.AudioFormat

interface SoundFile {
    fun getInputStream(): InputStream
    fun getName(): String
    fun getExtension(): String
    fun getAudioFormat(): AudioFormat
}