package liltojustice.trueadaptivemusic.client.sound.file

import liltojustice.trueadaptivemusic.client.TAMClient
import java.io.InputStream
import java.nio.file.Path
import javax.sound.sampled.AudioFormat
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.name

class RegularSoundFile(private val filePath: Path): SoundFile {
    private val audioFormat = TAMClient.getAudioFileFormat(filePath)

    override fun getInputStream(): InputStream {
        return filePath.inputStream()
    }

    override fun getName(): String {
        return filePath.name
    }

    override fun getExtension(): String {
        return filePath.extension
    }

    override fun getAudioFormat(): AudioFormat {
        return audioFormat
    }
}