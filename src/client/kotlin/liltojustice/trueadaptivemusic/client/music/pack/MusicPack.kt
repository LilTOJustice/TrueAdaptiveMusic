package liltojustice.trueadaptivemusic.client.music.pack

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.music.pack.meta.MusicPackMeta
import liltojustice.trueadaptivemusic.client.sound.SoundLibrary
import liltojustice.trueadaptivemusic.client.sound.file.RegularSoundFile
import liltojustice.trueadaptivemusic.client.sound.file.ZipSoundFile
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSoundDirectory
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSoundFile
import liltojustice.trueadaptivemusic.client.trigger.event.ErrorEvent
import liltojustice.trueadaptivemusic.client.trigger.predicate.ErrorPredicate
import liltojustice.trueadaptivemusic.client.music.tree.MusicTree
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.text.Text
import net.minecraft.util.JsonHelper
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.io.path.*

class MusicPack private constructor(
    var options: MusicPackOptions,
    var meta: MusicPackMeta,
    val rules: MusicTree,
    val packName: String,
    preValidation: MusicPackValidation? = null) {
    private val packPath = Path(Constants.MUSIC_PACK_DIR.pathString, packName)
    private val validation = MusicPackValidation(preValidation)

    val validationMessages
        get() = validation.toList()

    val isValid
        get() = validation.isValid()

    fun initEdit(packWithAssets: MusicPack? = null): Path {
        val packDir = getEditPackDir()
        if (!packDir.exists()) {
            packDir.createDirectory()
        }

        val assetsDir = Path(packDir.pathString, Constants.ASSETS_DIRNAME)
        if (!assetsDir.exists()) {
            assetsDir.createDirectory()
            if (packWithAssets?.isZipped() == true) {
                ZipFile(
                    Path(
                        Constants.MUSIC_PACK_DIR.pathString, packWithAssets.packName).pathString)
                    .use { zipFile ->
                        zipFile.entries().toList().filter { entry -> isZipAsset(entry.name) }
                            .forEach { entry ->
                                val path = Path(
                                    assetsDir.pathString,
                                    *Path(entry.name).drop(1).map { it.name }.toTypedArray())
                                path.createParentDirectories()
                                if (path.isDirectory()) {
                                    return@forEach
                                }

                                FileOutputStream(path.pathString)
                                    .use { out -> zipFile.getInputStream(entry).use { stream -> stream.copyTo(out) } }
                            }
                    }
            }
            else if (packWithAssets != null) {
                val existingAssets = Path(
                    Constants.MUSIC_PACK_DIR.pathString,
                    packWithAssets.packName,
                    Constants.ASSETS_DIRNAME
                )

                if (existingAssets.exists()) {
                    existingAssets.listDirectoryEntries().forEach { toCopy -> toCopy.copyTo(assetsDir) }
                }
            }
        }

        initRules()
        initOptions()

        return packDir
    }

    fun getEditPackAssetsPath(): Path {
        return Path(getEditPackDir().pathString, Constants.ASSETS_DIRNAME)
    }

    fun getEditPackSoundLibrary(): SoundLibrary {
        return getEditPackAssetsPath().listDirectoryEntriesRecursive()
            .map { file -> makePlayableSound(file) }
            .associateBy { file -> file.getSoundName() }
    }

    private fun getZipAssetNames(): List<String> {
        return ZipFile(packPath.toFile()).use { zipFile ->
            zipFile
                .entries()
                .toList()
                .filter { entry -> isZipAsset(entry.name) }
                .map { entry -> Path(entry.name).name }
        }
    }

    private fun getDirAssetNames(): List<String> {
        return Path(packPath.pathString, Constants.ASSETS_DIRNAME)
            .toFile()
            .listFiles()
            ?.map { file -> file.name }
            ?: emptyList()
    }

    private fun getPackAssetNames(): List<String> {
        return if (packPath.extension == "zip") {
            getZipAssetNames()
        } else {
            getDirAssetNames()
        }
    }

    fun initRules() {
        initMeta()
        val rulesFile = Path(getEditPackDir().pathString, Constants.RULES_FILENAME)

        if (!rulesFile.exists()) {
            rulesFile.createFile()
        }

        rulesFile.writeText(getGson().toJson(rules.toJson()))
    }

    fun initMeta() {
        meta = MusicPackMeta.init(rules)
        val metaFile = Path(getEditPackDir().pathString, Constants.META_FILENAME)

        if (!metaFile.exists()) {
            metaFile.createFile()
        }

        metaFile.writeText(meta.jsonEncode())
    }

    fun initOptions() {
        val optionsFile = Path(getEditPackDir().pathString, Constants.PACK_OPTIONS_FILENAME)

        if (!optionsFile.exists()) {
            optionsFile.createFile()
        }

        optionsFile.writeText(options.jsonEncode())
    }

    @OptIn(ExperimentalPathApi::class)
    fun save(): Path {
        val packOngoingDir = Path(
            Constants.MUSIC_PACK_DIR.pathString, "${Path(packName).nameWithoutExtension}.new")
        val packDir = Path(
            Constants.MUSIC_PACK_DIR.pathString, Path(packName).nameWithoutExtension)
        val assetsDir = Path(packOngoingDir.pathString, Constants.ASSETS_DIRNAME)
        val rulesFile = Path(packOngoingDir.pathString, Constants.RULES_FILENAME)
        val metaFile = Path(packOngoingDir.pathString, Constants.META_FILENAME)
        val optionsFile = Path(packOngoingDir.pathString, Constants.PACK_OPTIONS_FILENAME)

        val gson = GsonBuilder().setPrettyPrinting().create()
        rulesFile.toFile().writeText(gson.toJson(rules.toJson()))
        metaFile.toFile().writeText(meta.jsonEncode())
        optionsFile.toFile().writeText(options.jsonEncode())

        val outputPath = Path(packDir.pathString + ".zip")
        val newZipPath = Path(outputPath.pathString + ".new")

        val newZip = newZipPath.createFile()
        FileOutputStream(newZip.pathString).use { file ->
            ZipOutputStream(file).use { out ->
                out.putNextEntry(ZipEntry(rulesFile.name))
                rulesFile.inputStream().use { it.copyTo(out) }
                out.putNextEntry(ZipEntry(metaFile.name))
                metaFile.inputStream().use { it.copyTo(out) }
                out.putNextEntry(ZipEntry(optionsFile.name))
                optionsFile.inputStream().use { it.copyTo(out) }

                assetsDir.listDirectoryEntriesRecursive().forEach { entry ->
                    out.putNextEntry(
                        ZipEntry(
                            Path(
                                Constants.ASSETS_DIRNAME,
                                *entry.drop(3).map { it.name }.toTypedArray()
                            ).invariantSeparatorsPathString + if (entry.isDirectory()) PATH_SEPARATOR else ""
                        )
                    )

                    if (entry.isDirectory()) {
                        out.closeEntry()
                    }
                    else {
                        entry.inputStream().use { it.copyTo(out) }
                    }
                }
            }
        }

        try {
            Files.move(newZip, outputPath, StandardCopyOption.REPLACE_EXISTING)
            packOngoingDir.deleteRecursively()
        }
        catch (e: Exception) {
            TAMClient.errorToast(
                Text.translatableWithFallback(
                    "trueadaptivemusic.export_failure", "Failed to export pack! Try again."),
                e.message
            )
            Logger.logError("Failed to export to zip!")
            newZip.deleteIfExists()
        }

        return outputPath
    }

    private fun performStaticValidation() {
        val nonOggFiles = getPackAssetNames().filter { name -> Path(name).extension != "ogg" }
        if (!TAMClient.hasFFmpeg && nonOggFiles.isNotEmpty()) {
            validation.addWarning(
                Text.translatableWithFallback(
                    "trueadaptivemusic.ogg_warning",
                    "This pack contains music that is not 'ogg' type (the only type supported by " +
                            "minecraft). This music will not play unless FFmpeg is installed on your system. You " +
                            "can install it at the top right of your screen. If you already did, you may just need " +
                            "to restart your system."
                ).string
            )
        }


        if (isZipped()) {
            val zipFile = ZipFile(packPath.toFile())
            if (zipFile.entries().toList().any { it.name.contains("\\") }) {
                validation.addWarning(
                    "This pack has not been zipped properly, likely because it is old. " +
                            "If you are the pack creator, you should re-export it before releasing it."
                )
            }
        }

        val loader = FabricLoader.getInstance()
        meta.requiredBridgeMods.forEach { mod ->
            if (!loader.isModLoaded(mod.id)) {
                validation.addWarning("This pack requires the mod ${mod.name} (${mod.id}) which could not be found.")
            }
        }

        rules.traverse { node, _ ->
            node.predicates.forEach { predicate ->
                (predicate as? ErrorPredicate)?.let {
                    validation.addWarning(it.reason)
                }
            }

            node.events.forEach { event ->
                (event as? ErrorEvent)?.let {
                    validation.addWarning(it.reason)
                }
            }
        }
    }

    private fun getEditPackDir(): Path {
        return Path(
            Constants.MUSIC_PACK_DIR.pathString, "${Path(packName).nameWithoutExtension}.new")
    }

    private fun isZipped(): Boolean {
        return Path(packName).extension == "zip"
    }

    companion object {
        private val jsonErrorText =
            Text.translatableWithFallback(
                "trueadaptivemusic.json_error",
                "Could not load pack due to json error:"
            ).string

        fun loadAllPacks(): List<MusicPack> {
            return Constants.MUSIC_PACK_DIR.listDirectoryEntries().mapNotNull { path ->
                try {
                    return@mapNotNull fromFile(path)
                }
                catch (e: Exception) {
                    Logger.logError("Failed to load pack from path $path:\n${e}")
                }

                return@mapNotNull null
            }
        }

        fun makeEmpty(packName: String): MusicPack {
            return MusicPack(MusicPackOptions(), MusicPackMeta(), MusicTree.makeEmpty(), packName)
        }

        fun fromFile(filePath: Path): MusicPack? {
            val zip = filePath.extension == "zip"
            if (!zip && !filePath.isDirectory()) {
                Logger.logWarning("Could not find music pack $filePath.")
                return null
            }

            try {
                val pack = if (zip) fromZipFile(filePath) else fromDirectory(filePath)
                pack.performStaticValidation()

                return pack
            }
            catch (e: Exception) {
                throw MusicLoadException("Failed to read music pack: $filePath", e)
            }
        }

        private fun getGson(): Gson {
            return GsonBuilder().setPrettyPrinting().create()
        }

        private fun fromDirectory(filePath: Path): MusicPack {
            val files = filePath.listDirectoryEntries()
            var meta = MusicPackMeta()
            var options = MusicPackOptions()
            val assetsDir = files.find { file -> file.fileName.name == Constants.ASSETS_DIRNAME }
            if (assetsDir == null) {
                Logger.logInfo(
                    "Assets dir ${Constants.ASSETS_DIRNAME} is missing, so no external music will be used")
            }

            val playableSounds = assetsDir?.listDirectoryEntriesRecursive()
                ?.map { file -> makePlayableSound(file) }
                ?.associateBy { file -> file.getSoundName() } ?: mapOf()
            val rulesFile = files.find { file -> file.fileName.name == Constants.RULES_FILENAME }
            val metaFile = files.find { file -> file.fileName.name == Constants.META_FILENAME }
            val optionsFile = files.find { file -> file.fileName.name == Constants.PACK_OPTIONS_FILENAME }

            if (metaFile != null) {
                meta = MusicPackMeta.jsonDecode(metaFile.inputStream().reader().readText())
            }

            if (optionsFile != null) {
                options = MusicPackOptions.jsonDecode(optionsFile.inputStream().reader().readText())
            }

            if (rulesFile == null) {
                throw MusicLoadException(
                    "Rules file \"${Constants.RULES_FILENAME}\" not found in pack ${filePath.name}"
                )
            }

            val preValidation = MusicPackValidation()

            val rules = try {
                MusicTree.fromJson(
                    JsonHelper.deserialize(rulesFile.inputStream().reader()), playableSounds)
            }
            catch (e: JsonParseException) {
                preValidation.addError("$jsonErrorText\n$e")
                MusicTree.makeEmpty()
            }

            return MusicPack(
                options,
                meta,
                rules,
                filePath.name,
                preValidation
            )
        }

        private fun fromZipFile(filePath: Path): MusicPack {
            ZipFile(filePath.toFile()).use { zipFile ->
                val files = zipFile.entries().toList()
                var meta = MusicPackMeta()
                var options = MusicPackOptions()
                val playableSounds = files
                    .filter { file -> isZipAsset(file.name) }
                    .map { file -> makePlayableSounds(filePath, file, files) }
                    .associateBy { file -> file.getSoundName() }
                val rulesFile = files.find { file -> Path(file.name).fileName.name == Constants.RULES_FILENAME }
                val metaFile = files.find { file -> Path(file.name).fileName.name == Constants.META_FILENAME }
                val optionsFile = files
                    .find { file -> Path(file.name).fileName.name == Constants.PACK_OPTIONS_FILENAME }

                if (metaFile != null) {
                    zipFile.getInputStream(metaFile).use {
                        meta = MusicPackMeta.jsonDecode(it.reader().readText())
                    }
                }

                if (optionsFile != null) {
                    zipFile.getInputStream(optionsFile).use {
                        options = MusicPackOptions.jsonDecode(it.reader().readText())
                    }
                }

                if (rulesFile == null) {
                    throw MusicLoadException(
                        "Rules file \"${Constants.RULES_FILENAME}\" not found in pack ${filePath.name}"
                    )
                }

                val preValidation = MusicPackValidation()

                val rules = try {
                    zipFile.getInputStream(rulesFile).use {
                        MusicTree.fromJson(
                            JsonHelper.deserialize(it.reader()) , playableSounds)
                    }
                }
                catch (e: JsonParseException) {
                    preValidation.addError("$jsonErrorText\n$e")
                    MusicTree.makeEmpty()
                }

                return MusicPack(
                    options,
                    meta,
                    rules,
                    filePath.name,
                    preValidation
                )
            }
        }

        private fun isZipAsset(fileName: String): Boolean {
            return fileName.startsWith(Constants.ASSETS_DIRNAME)
        }

        private fun makePlayableSound(filePath: Path): PlayableSound {
            return if (filePath.isDirectory()) {
                PlayableSoundDirectory(
                    filePath.name,
                    filePath.listDirectoryEntriesRecursive()
                        .filter { !it.isDirectory() }
                        .map { RegularSoundFile(it) }
                )
            }
            else {
                PlayableSoundFile(RegularSoundFile(filePath))
            }
        }

        private fun makePlayableSounds(
            zipFilePath: Path, zipEntry: ZipEntry, zipEntries: List<ZipEntry>): PlayableSound {
            return if (zipEntry.isActuallyDirectory) {
                PlayableSoundDirectory(
                    Path(zipEntry.name).name,
                    zipEntries
                        .filter { it.name.startsWith(zipEntry.name) && !it.isActuallyDirectory }
                        .map {
                            ZipSoundFile(
                                zipFilePath, Path(it.name.replace("\\", "/")))
                        }
                )
            }
            else {
                PlayableSoundFile(
                    ZipSoundFile(
                        zipFilePath, Path(zipEntry.name.replace("\\", "/")))
                )
            }
        }
    }
}