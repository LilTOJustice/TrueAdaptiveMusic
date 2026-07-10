package liltojustice.trueadaptivemusic.client.sound.file

import liltojustice.trueadaptivemusic.client.music.pack.MusicLoadException
import liltojustice.trueadaptivemusic.client.sound.stream.ZipInputStream
import java.nio.file.Path
import java.util.zip.ZipFile
import kotlin.io.path.extension
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.name

class ZipSoundFile(private val zipFilePath: Path, private val zipEntryPath: Path): SoundFile {
    val zipEntry = run {
        ZipFile(zipFilePath.toFile()).use { zipFile ->
            val trueEntryPath = zipEntryPath.invariantSeparatorsPathString
            zipFile.getEntry(trueEntryPath)
                ?: zipFile.entries().toList().firstOrNull { it.name.replace("\\", "/") == trueEntryPath }
        }
    }

    override fun getInputStream(): ZipInputStream {
        val entry = zipEntry
            ?: throw MusicLoadException(
                "Could not load zip entry $zipEntryPath from zip file ${zipFilePath.name}")

        return ZipInputStream(zipFilePath, entry)
    }

    override fun getName(): String {
        return zipEntryPath.name
    }

    override fun getExtension(): String {
        return zipEntryPath.extension
    }
}