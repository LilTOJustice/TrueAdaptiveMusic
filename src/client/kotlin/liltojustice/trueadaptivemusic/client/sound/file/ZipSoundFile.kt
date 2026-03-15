package liltojustice.trueadaptivemusic.client.sound.file

import liltojustice.trueadaptivemusic.client.music.pack.MusicLoadException
import java.io.InputStream
import java.nio.file.Path
import java.util.zip.ZipFile
import kotlin.io.path.extension
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.name

class ZipSoundFile(zipFilePath: Path, private val zipEntryPath: Path): SoundFile {
    val zipFile = ZipFile(zipFilePath.toFile())
    val zipEntry = run {
        val trueEntryPath = zipEntryPath.invariantSeparatorsPathString
        zipFile.getEntry(trueEntryPath)
            ?: zipFile.entries().toList().firstOrNull { it.name.replace("\\", "/") == trueEntryPath }
    }

    override fun getInputStream(): InputStream {
        val entry = zipEntry
            ?: throw MusicLoadException("Could not load zip entry $zipEntryPath from zip file ${zipFile.name}")

        return zipFile.getInputStream(entry)
    }

    override fun getName(): String {
        return zipEntryPath.name
    }

    override fun getExtension(): String {
        return zipEntryPath.extension
    }
}