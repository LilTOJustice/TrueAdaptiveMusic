package liltojustice.trueadaptivemusic.client.sound.file

import liltojustice.trueadaptivemusic.ZipInputStream
import liltojustice.trueadaptivemusic.client.TAMClient
import java.io.InputStream
import java.nio.file.Path
import java.util.zip.ZipFile
import javax.sound.sampled.AudioFormat
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.pathString

class ZipSoundFile(zipFilePath: Path, private val zipEntryPath: Path): SoundFile {
    private val zipFile = ZipFile(zipFilePath.toFile())
    private val zipEntry = zipFile.getEntry(zipEntryPath.pathString)
    private val audioFormat = TAMClient.getAudioFileFormat(zipFile, zipEntry)

    override fun getInputStream(): InputStream {
        return ZipInputStream(zipFile, zipEntry)
    }

    override fun getName(): String {
        return zipEntryPath.name
    }

    override fun getExtension(): String {
        return zipEntryPath.extension
    }

    override fun getAudioFormat(): AudioFormat {
        return audioFormat
    }
}