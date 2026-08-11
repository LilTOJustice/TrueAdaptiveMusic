package liltojustice.trueadaptivemusicextensions.client

import liltojustice.trueadaptivemusic.Constants.FFMPEG_DIR
import liltojustice.trueadaptivemusic.Constants.OPTIONS_DIR
import liltojustice.trueadaptivemusic.Constants.PACK_BROWSER_CACHE_DIR
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.Path
import kotlin.io.path.invariantSeparatorsPathString

object ExtensionConstants {
    const val FFMPEG_WINDOWS_RESOURCE = "assets/trueadaptivemusicextensions/ffmpeg/ffmpeg.exe"
    const val FFPROBE_WINDOWS_RESOURCE = "assets/trueadaptivemusicextensions/ffmpeg/ffprobe.exe"
    const val LIBWINPTHREAD_WINDOWS_RESOURCE = "assets/trueadaptivemusicextensions/ffmpeg/libwinpthread-1.dll"
    const val GIGA_GRABBER_WINDOWS_RESOURCE = "assets/trueadaptivemusicextensions/gigagrabber/giga_grabber.exe"
    const val FFMPEG_RESOURCE = "assets/trueadaptivemusicextensions/ffmpeg/ffmpeg"
    const val FFPROBE_RESOURCE = "assets/trueadaptivemusicextensions/ffmpeg/ffprobe"
    const val GIGA_GRABBER_RESOURCE = "assets/trueadaptivemusicextensions/gigagrabber/giga_grabber.exe"
    val POSIX_PERMISSIONS: FileAttribute<Set<PosixFilePermission>> = PosixFilePermissions.asFileAttribute(
        PosixFilePermissions.fromString("rwxrwxrwx"))
    val GIGA_GRABBER_DIR = Path(OPTIONS_DIR.invariantSeparatorsPathString, "giga_grabber_binaries")
    val MANIFEST_PATH = Path(
        PACK_BROWSER_CACHE_DIR.invariantSeparatorsPathString, "manifest.json")
    val MANIFEST_PATH_TEMP = Path(
        PACK_BROWSER_CACHE_DIR.invariantSeparatorsPathString, "manifest.json.tmp")
    val LIBWINPTHREAD_WINDOWS_PATH = Path(
        FFMPEG_DIR.invariantSeparatorsPathString, "libwinpthread-1.dll")
    val GIGA_GRABBER_WINDOWS_PATH = Path(
        GIGA_GRABBER_DIR.invariantSeparatorsPathString, "giga_grabber.exe")
    val GIGA_GRABBER_PATH = Path(
        GIGA_GRABBER_DIR.invariantSeparatorsPathString, "giga_grabber")
}