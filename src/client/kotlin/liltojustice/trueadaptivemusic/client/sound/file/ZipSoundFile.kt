package liltojustice.trueadaptivemusic.client.sound.file

import java.io.InputStream
import java.nio.file.Path
import java.util.zip.ZipFile
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.pathString

class ZipSoundFile(private val zipFilePath: Path, private val zipEntryPath: Path): SoundFile {
    override fun getInputStream(): InputStream {
        val zipFile = ZipFile(zipFilePath.toFile())
        val zipEntry = zipFile.getEntry(zipEntryPath.pathString)
        return zipFile.getInputStream(zipEntry)
    }

    override fun getName(): String {
        return zipEntryPath.name
    }

    override fun getExtension(): String {
        return zipEntryPath.extension
    }
}