package liltojustice.trueadaptivemusic.common.client.sound.stream

import java.io.InputStream
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class ZipInputStream(zipFilePath: Path, zipEntry: ZipEntry): InputStream() {
    private val zipFile = ZipFile(zipFilePath.toFile())
    private val internalStream = zipFile.getInputStream(zipEntry)

    override fun read(): Int {
        return internalStream.read()
    }

    override fun close() {
        internalStream.close()
        zipFile.close()
        super.close()
    }
}