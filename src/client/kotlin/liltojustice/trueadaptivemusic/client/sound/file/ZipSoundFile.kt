package liltojustice.trueadaptivemusic.client.sound.file

import liltojustice.trueadaptivemusic.client.music.pack.MusicLoadException
import java.io.InputStream
import java.nio.file.Path
import java.util.zip.ZipFile
import kotlin.io.path.extension
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.name

class ZipSoundFile(private val zipFilePath: Path, private val zipEntryPath: Path): SoundFile {
    override fun getInputStream(): InputStream {
        val zipFile = ZipFile(zipFilePath.toFile())
        val trueEntryPath = zipEntryPath.invariantSeparatorsPathString
        val zipEntry = zipFile.getEntry(trueEntryPath)

        return zipFile.getInputStream(
            zipEntry
                ?: zipFile
                    .entries().toList().firstOrNull { it.name.replace("\\", "/") == trueEntryPath }
                ?: throw MusicLoadException(
                    "Could not load zip entry $trueEntryPath from zip file ${zipFile.name}")
        )
    }

    override fun getName(): String {
        return zipEntryPath.name
    }

    override fun getExtension(): String {
        return zipEntryPath.extension
    }
}