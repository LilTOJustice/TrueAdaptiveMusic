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
    private const val EXTENSIONS_ROOT = "assets/trueadaptivemusicextensions"
    private const val FFMPEG_ROOT = "$EXTENSIONS_ROOT/ffmpeg"
    private const val GIGA_GRABBER_ROOT = "$EXTENSIONS_ROOT/gigagrabber"
    const val FFMPEG_WINDOWS_RESOURCE = "$FFMPEG_ROOT/ffmpeg.exe"
    const val FFPROBE_WINDOWS_RESOURCE = "$FFMPEG_ROOT/ffprobe.exe"
    const val LIBWINPTHREAD_WINDOWS_RESOURCE = "$FFMPEG_ROOT/libwinpthread-1.dll"
    const val GIGA_GRABBER_WINDOWS_RESOURCE = "$GIGA_GRABBER_ROOT/giga_grabber.exe"
    const val FFMPEG_LINUX_RESOURCE = "$FFMPEG_ROOT/ffmpeg_linux"
    const val FFPROBE_LINUX_RESOURCE = "$FFMPEG_ROOT/ffprobe_linux"
    const val GIGA_GRABBER_LINUX_RESOURCE = "$GIGA_GRABBER_ROOT/giga_grabber_linux"
    const val FFMPEG_MAC_RESOURCE= "$FFMPEG_ROOT/ffmpeg_mac"
    const val FFPROBE_MAC_RESOURCE = "$FFMPEG_ROOT/ffprobe_mac"
    const val GIGA_GRABBER_MAC_RESOURCE = "$GIGA_GRABBER_ROOT/giga_grabber_mac"
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
        GIGA_GRABBER_DIR.invariantSeparatorsPathString, "giga_grabber_linux.exe")
    val GIGA_GRABBER_PATH = Path(
        GIGA_GRABBER_DIR.invariantSeparatorsPathString, "giga_grabber_linux")
}