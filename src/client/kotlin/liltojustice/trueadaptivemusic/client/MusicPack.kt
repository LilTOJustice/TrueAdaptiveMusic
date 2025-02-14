package liltojustice.trueadaptivemusic.client

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicateTree
import liltojustice.trueadaptivemusic.client.sound.PlayableSoundFile
import liltojustice.trueadaptivemusic.client.sound.RegularSoundFile
import liltojustice.trueadaptivemusic.client.sound.ZipSoundFile
import net.minecraft.util.JsonHelper
import java.nio.file.Path
import java.util.zip.ZipFile
import kotlin.io.path.*

class MusicPack private constructor(val path: Path, val metadata: Metadata, val rules: MusicPredicateTree) {
    companion object {
        fun fromFile(filePath: Path): MusicPack {
            val zip = filePath.extension == "zip"
            if (!zip && !filePath.isDirectory()) {
                throw MusicLoadException("Given path \"$filePath\" is neither a directory nor a zip file")
            }

            try {
                return if (zip) fromZipFile(filePath) else fromDirectory(filePath)
            }
            catch (e: Exception) {
                throw MusicLoadException("Failed to read music pack: ${filePath}:\nInner Exception:\n${e}")
            }
        }

        private fun getLeafFiles(files: List<Path>): List<Path> {
            return files.flatMap { path ->
                if (path.isDirectory()) getLeafFiles(path.listDirectoryEntries()) else listOf(path) }
        }

        private fun fromDirectory(filePath: Path): MusicPack {
            val files = getLeafFiles(filePath.listDirectoryEntries())
            var metadata = Metadata(filePath.name, "")
            val playableSoundFiles = files.filter { file -> file.extension === "ogg" }
                .map { file -> PlayableSoundFile(RegularSoundFile(file))}
                .associateBy { file -> file.getSoundName() }
            val rulesFile = files.find { file -> file.fileName.name == Constants.RULES_FILENAME }
            val metaFile = files.find { file -> file.fileName.name == Constants.META_FILENAME }

            if (metaFile != null)
            {
                metadata = Metadata.fromJson(JsonHelper.deserialize(metaFile.inputStream().reader()))
            }

            if (rulesFile == null)
            {
                throw MusicLoadException(
                    "Rules file \"${Constants.RULES_FILENAME}\" not found in pack ${filePath.name}")
            }

            return MusicPack(
                filePath,
                metadata,
                MusicPredicateTree.fromJson(
                    JsonHelper.deserialize(rulesFile.inputStream().reader()), playableSoundFiles)
            )
        }

        private fun fromZipFile(filePath: Path): MusicPack {
            val zipFile = ZipFile(filePath.toFile())
            val files = zipFile.entries().toList().filter { file -> !file.isDirectory }
            var metadata = Metadata(filePath.name, "")
            val playableSoundFiles = files.filter { file -> Path(file.name).extension == "ogg" }
                .map { file -> PlayableSoundFile(ZipSoundFile(zipFile, file))}
                .associateBy { file -> file.getSoundName() }
            val rulesFile = files.find { file -> Path(file.name).fileName.name == Constants.RULES_FILENAME }
            val metaFile = files.find { file -> Path(file.name).fileName.name == Constants.META_FILENAME }

            if (metaFile != null)
            {
                metadata = Metadata.fromJson(JsonHelper.deserialize(zipFile.getInputStream(metaFile).reader()))
            }

            if (rulesFile == null)
            {
                throw MusicLoadException(
                    "Rules file \"${Constants.RULES_FILENAME}\" not found in pack ${filePath.name}")
            }

            return MusicPack(
                filePath,
                metadata,
                MusicPredicateTree.fromJson(
                    JsonHelper.deserialize(zipFile.getInputStream(rulesFile).reader()), playableSoundFiles)
            )
        }
    }

    data class Metadata(val name: String, val description: String) {
        companion object {
            fun fromJson(json: JsonObject): Metadata {
                return Metadata(
                    json.getAsJsonPrimitive("name")?.asString ?: "",
                    json.getAsJsonPrimitive("description")?.asString ?: "")
            }
        }
    }
}

