package liltojustice.trueadaptivemusic.client

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicateTree
import liltojustice.trueadaptivemusic.client.sound.PlayableSoundFile
import liltojustice.trueadaptivemusic.client.sound.RegularSoundFile
import liltojustice.trueadaptivemusic.client.sound.ZipSoundFile
import net.minecraft.util.JsonHelper
import java.io.FileOutputStream
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.io.path.*

class MusicPack private constructor(val metadata: Metadata, val rules: MusicPredicateTree, val packName: String) {
    fun copy(): MusicPack {
        return MusicPack(metadata.copy(), rules.copy(), packName)
    }

    fun initEdit(forNew: Boolean = false) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val packDir = Path(
            Constants.MUSIC_PACK_DIR,
            Path(packName).nameWithoutExtension + if (forNew) ".new" else "")
        if (!packDir.exists()) {
            packDir.createDirectory()
        }

        val assetsDir = Path(packDir.pathString, Constants.ASSETS_DIRNAME)
        if (!assetsDir.exists()) {
            assetsDir.createDirectory()
        }

        val rulesFile = Path(packDir.pathString, Constants.RULES_FILENAME)
        if (!rulesFile.exists()) {
            rulesFile.createFile()
        }
        rulesFile.writeText(gson.toJson(rules.toJson()))

        val metaFile = Path(packDir.pathString, Constants.META_FILENAME)
        if (!metaFile.exists()) {
            metaFile.createFile()
        }
        metaFile.writeText(gson.toJson(metadata.toJson()))
    }

    @OptIn(ExperimentalPathApi::class)
    fun save() {
        val packOngoingDir = Path(Constants.MUSIC_PACK_DIR, "${Path(packName).nameWithoutExtension}.new")
        val packDir = Path(Constants.MUSIC_PACK_DIR, Path(packName).nameWithoutExtension)
        val assetsDir = Path(packOngoingDir.pathString, Constants.ASSETS_DIRNAME)
        val rulesFile = Path(packOngoingDir.pathString, Constants.RULES_FILENAME)
        val metaFile = Path(packOngoingDir.pathString, Constants.META_FILENAME)
        val gson = GsonBuilder().setPrettyPrinting().create()
        rulesFile.toFile().writeText(gson.toJson(rules.toJson()))
        metaFile.toFile().writeText(gson.toJson(metadata.toJson()))
        ZipOutputStream(FileOutputStream(Path(packDir.pathString + ".zip").createFile().pathString)).use { out ->
            out.putNextEntry(ZipEntry(rulesFile.name))
            rulesFile.inputStream().copyTo(out)
            out.putNextEntry(ZipEntry(metaFile.name))
            metaFile.inputStream().copyTo(out)
            assetsDir.listDirectoryEntries().forEach { entry ->
                out.putNextEntry(ZipEntry(entry.name))
                entry.inputStream().copyTo(out)
            }
        }
        packOngoingDir.deleteRecursively()
    }

    companion object {
        fun makeEmpty(packName: String): MusicPack {
            return MusicPack(Metadata(), MusicPredicateTree.makeEmpty(), packName)
        }

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
            var metadata = Metadata()
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
                metadata,
                MusicPredicateTree.fromJson(
                    JsonHelper.deserialize(rulesFile.inputStream().reader()), playableSoundFiles),
                filePath.name
            )
        }

        private fun fromZipFile(filePath: Path): MusicPack {
            val zipFile = ZipFile(filePath.toFile())
            val files = zipFile.entries().toList()
            var metadata = Metadata()
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
                metadata,
                MusicPredicateTree.fromJson(
                    JsonHelper.deserialize(zipFile.getInputStream(rulesFile).reader()), playableSoundFiles),
                filePath.name
            )
        }
    }

    data class Metadata(var description: String = "") {
        fun toJson(): JsonObject {
            val result = JsonObject()
            result.add("description", JsonPrimitive(description))

            return result
        }

        companion object {
            fun fromJson(json: JsonObject): Metadata {
                return Metadata(
                    json.getAsJsonPrimitive("description")?.asString ?: "")
            }
        }
    }
}

