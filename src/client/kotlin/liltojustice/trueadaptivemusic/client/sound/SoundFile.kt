package liltojustice.trueadaptivemusic.client.sound

import java.io.InputStream

interface SoundFile {
    fun getInputStream(): InputStream
    fun getName(): String
    fun getExtension(): String
}