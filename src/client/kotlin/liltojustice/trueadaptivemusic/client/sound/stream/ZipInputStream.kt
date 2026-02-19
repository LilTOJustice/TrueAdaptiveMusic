package liltojustice.trueadaptivemusic.client.sound.stream

import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class ZipInputStream(zipFile: ZipFile, zipEntry: ZipEntry): InputStream() {
    private val internalStream = zipFile.getInputStream(zipEntry)

    override fun read(): Int {
        return internalStream.read()
    }

    override fun close() {
        internalStream.close()
        super.close()
    }
}