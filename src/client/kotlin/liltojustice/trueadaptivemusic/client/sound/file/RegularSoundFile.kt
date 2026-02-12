package liltojustice.trueadaptivemusic.client.sound.file

import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.name

class RegularSoundFile(private val filePath: Path): SoundFile {
    override fun getInputStream(): InputStream {
        return filePath.inputStream()
    }

    override fun getName(): String {
        return filePath.name
    }

    override fun getExtension(): String {
        return filePath.extension
    }
}