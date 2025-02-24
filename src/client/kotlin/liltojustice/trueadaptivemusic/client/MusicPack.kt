package liltojustice.trueadaptivemusic.client

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicateTree
import liltojustice.trueadaptivemusic.client.sound.*
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

    fun initEdit(packWithAssets: MusicPack? = null) {
        val packDir = getEditPackDir()
        if (!packDir.exists()) {
            packDir.createDirectory()
        }

        val assetsDir = Path(packDir.pathString, Constants.ASSETS_DIRNAME)
        if (!assetsDir.exists()) {
            assetsDir.createDirectory()
            if (packWithAssets?.isZipped() == true) {
                val zip = ZipFile(Path(Constants.MUSIC_PACK_DIR, packWithAssets.packName).pathString)
                zip.entries().toList().filter { entry -> Path(entry.name).extension == "ogg" }.forEach { entry ->
                    FileOutputStream(Path(assetsDir.pathString, Path(entry.name).name).pathString).use { out ->
                        zip.getInputStream(entry).use { stream -> stream.copyTo(out) }
                    }
                }
            }
            else if (packWithAssets != null) {
                val existingAssets = Path(Constants.MUSIC_PACK_DIR, packWithAssets.packName, Constants.ASSETS_DIRNAME)

                if (existingAssets.exists()) {
                    existingAssets.listDirectoryEntries().forEach { toCopy -> toCopy.copyTo(assetsDir) }
                }
            }
        }

        initRules()
        initMeta()
    }

    fun getEditPackAssetsPath(): Path {
        return Path(getEditPackDir().pathString, Constants.ASSETS_DIRNAME)
    }

    fun getEditPackAssets(): Map<String, PlayableSound> {
        return getEditPackAssetsPath().listDirectoryEntries()
            .filter { file -> file.extension == "ogg" }
            .map { file -> PlayableSoundFile(RegularSoundFile(file)) }
            .associateBy { file -> file.getSoundName() }
    }

    fun initRules() {
        val rulesFile = Path(getEditPackDir().pathString, Constants.RULES_FILENAME)

        if (!rulesFile.exists()) {
            rulesFile.createFile()
        }

        rulesFile.writeText(getGson().toJson(rules.toJson()))
    }

    fun initMeta() {
        val metaFile = Path(getEditPackDir().pathString, Constants.META_FILENAME)
        if (!metaFile.exists()) {
            metaFile.createFile()
        }
        metaFile.writeText(getGson().toJson(metadata.toJson()))
    }

    @OptIn(ExperimentalPathApi::class)
    fun save(): Path {
        val packOngoingDir = Path(Constants.MUSIC_PACK_DIR, "${Path(packName).nameWithoutExtension}.new")
        val packDir = Path(Constants.MUSIC_PACK_DIR, Path(packName).nameWithoutExtension)
        val assetsDir = Path(packOngoingDir.pathString, Constants.ASSETS_DIRNAME)
        val rulesFile = Path(packOngoingDir.pathString, Constants.RULES_FILENAME)
        val metaFile = Path(packOngoingDir.pathString, Constants.META_FILENAME)
        val gson = GsonBuilder().setPrettyPrinting().create()
        rulesFile.toFile().writeText(gson.toJson(rules.toJson()))
        metaFile.toFile().writeText(gson.toJson(metadata.toJson()))
        val outputPath = Path(packDir.pathString + ".zip")
        outputPath.deleteIfExists()
        ZipOutputStream(FileOutputStream(outputPath.createFile().pathString)).use { out ->
            out.putNextEntry(ZipEntry(rulesFile.name))
            rulesFile.inputStream().copyTo(out)
            out.putNextEntry(ZipEntry(metaFile.name))
            metaFile.inputStream().copyTo(out)
            assetsDir.listDirectoryEntries().forEach { entry ->
                out.putNextEntry(ZipEntry(Path(Constants.ASSETS_DIRNAME, entry.name).pathString))
                entry.inputStream().copyTo(out)
            }
        }
        packOngoingDir.deleteRecursively()

        return outputPath
    }

    private fun getGson(): Gson {
        return GsonBuilder().setPrettyPrinting().create()
    }

    private fun getEditPackDir(): Path {
        return Path(Constants.MUSIC_PACK_DIR, "${Path(packName).nameWithoutExtension}.new")
    }

    private fun isZipped(): Boolean {
        return Path(packName).extension == "zip"
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
                throw MusicLoadException("Failed to read music pack: $filePath", e)
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
                        Constants.ASSETS_DIRNAME + path.fileSystem.separator)
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

