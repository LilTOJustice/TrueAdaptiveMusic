package liltojustice.trueadaptivemusic.client.sound

import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import kotlin.io.path.Path
import kotlin.io.path.name

class ZipSoundFile(private val zipFile: ZipFile, private val zipEntry: ZipEntry) : SoundFile {
    override fun getInputStream(): InputStream {
        return zipFile.getInputStream(zipEntry)
    }

    override fun getName(): String {
        return Path(zipEntry.name).name
    }
}