package liltojustice.trueadaptivemusic.client.music.pack

import java.nio.file.Path
import java.util.zip.ZipEntry
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

internal val ZipEntry.isActuallyDirectory: Boolean get() =
    isDirectory || name.endsWith(PATH_SEPARATOR) || name.endsWith("\\")

internal const val PATH_SEPARATOR = "/"

internal fun Path.listDirectoryEntriesRecursive(includeRoot: Boolean = false, includeDirectories: Boolean = true): List<Path> {
    val thisList = listOf(this).takeIf { includeRoot && includeDirectories } ?: emptyList()
    if (!isDirectory()) {
        return listOf(this)
    }

    return listDirectoryEntries().flatMap { it.listDirectoryEntriesRecursive(true, includeDirectories) } + thisList
}
