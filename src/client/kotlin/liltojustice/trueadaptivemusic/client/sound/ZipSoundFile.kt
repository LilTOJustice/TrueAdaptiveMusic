package liltojustice.trueadaptivemusic.client.sound

import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class ZipSoundFile(private val zipFile: ZipFile, private val zipEntry: ZipEntry) : SoundFile {
    override fun getInputStream(): InputStream {
        return zipFile.getInputStream(zipEntry)
    }

    override fun getName(): String {
        return zipFile.name
    }
}