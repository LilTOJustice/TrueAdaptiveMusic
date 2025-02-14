package liltojustice.trueadaptivemusic.client

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.Logger
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

        private fun fromDirectory(filePath: Path): MusicPack {
            val files = filePath.listDirectoryEntries()
            var metadata = Metadata(filePath.name, "")
            val assetsDir = files.find { file -> file.fileName.name == Constants.ASSETS_DIRNAME }
            if (assetsDir == null)
            {
                Logger.log(
                    "Assets dir ${Constants.ASSETS_DIRNAME} is missing, so no external music will be used")
            }
            val playableSoundFiles = assetsDir?.listDirectoryEntries()
                ?.filter { file -> file.extension == "ogg" }
                ?.map { file -> PlayableSoundFile(RegularSoundFile(file)) }
                ?.associateBy { file -> file.getSoundName() } ?: mapOf()
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
            val files = zipFile.entries().toList()
            var metadata = Metadata(filePath.name, "")
            val playableSoundFiles = files
                .filter { file ->
                    val path = Path(file.name)
                    return@filter path.extension == "ogg" && file.name.contains(
                        Constants.ASSETS_DIRNAME + '/')
                }
                .map { file -> PlayableSoundFile(ZipSoundFile(zipFile, file)) }
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

