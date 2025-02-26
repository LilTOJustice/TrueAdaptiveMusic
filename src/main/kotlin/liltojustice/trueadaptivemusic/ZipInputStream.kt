package liltojustice.trueadaptivemusic

import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class ZipInputStream(private val zipFile: ZipFile, zipEntry: ZipEntry): InputStream() {
    private val internalStream = zipFile.getInputStream(zipEntry)

    override fun read(): Int {
        return internalStream.read()
    }

    override fun close() {
        zipFile.close()
        super.close()
    }
}