package liltojustice.trueadaptivemusic.client.music.pack

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.Reference
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.music.pack.meta.MusicPackMeta
import liltojustice.trueadaptivemusic.client.sound.SoundLibrary
import liltojustice.trueadaptivemusic.client.sound.file.RegularSoundFile
import liltojustice.trueadaptivemusic.client.sound.file.ZipSoundFile
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSoundDirectory
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSoundFile
import liltojustice.trueadaptivemusic.client.music.tree.MusicTree
import liltojustice.trueadaptivemusic.client.sound.stream.ZipInputStream
import liltojustice.trueadaptivemusic.client.trigger.event.ErrorEvent
import liltojustice.trueadaptivemusic.client.trigger.predicate.ErrorPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.Component
import net.minecraft.util.GsonHelper
import java.io.FileOutputStream
import java.io.InputStream
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
    preValidation: MusicPackValidation? = null
) {
    val packPath = Path(Constants.MUSIC_PACK_DIR.invariantSeparatorsPathString, packName)
    val isZip = packPath.extension == "zip"

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
            if (packWithAssets?.isZip == true) {
                ZipFile(packWithAssets.packPath.invariantSeparatorsPathString).use { zipFile ->
                    zipFile.entries().toList().filter { entry -> isZipAsset(entry.name) }
                        .forEach { entry ->
                            val path = Path(
                                assetsDir.pathString,
                                *Path(entry.name).drop(1).map { it.name }.toTypedArray()
                            )
                            if (entry.isDirectory) {
                                return@forEach
                            }

                            path.createParentDirectories()

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

        val predicatesDir = Path(packDir.pathString, Constants.PREDICATES_DIRNAME)
        if (!predicatesDir.exists()) {
            predicatesDir.createDirectory()
            if (packWithAssets?.isZip == true) {
                ZipFile(packWithAssets.packPath.invariantSeparatorsPathString).use { zipFile ->
                    zipFile.entries().toList()
                        .filter { it.name.contains("/${Constants.PREDICATES_DIRNAME}/") }.forEach { entry ->
                            val path = Path(
                                predicatesDir.pathString,
                                *Path(entry.name).drop(1).map { it.name }.toTypedArray()
                            )
                            path.createParentDirectories()
                            if (path.isDirectory()) {
                                return@forEach
                            }

                            FileOutputStream(path.pathString)
                                .use { out -> zipFile.getInputStream(entry).use { stream -> stream.copyTo(out) } }
                        }
                }
            }
        }

        val iconFile = Path(packDir.invariantSeparatorsPathString, Constants.ICON_FILENAME)
        if (!iconFile.exists()) {
            getIconStream(false)?.use { iconStream ->
                iconFile.createFile().toFile().outputStream().use {
                    iconStream.copyTo(it)
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

    fun getIconStream(allowDefault: Boolean = true): InputStream? {
        return (if (isZip) {
            ZipFile(packPath.toFile()).use { zipFile ->
                zipFile.entries().toList().firstOrNull { it.name == Constants.ICON_FILENAME }
            }?.let {
                ZipInputStream(packPath, it)
            }
        }
        else {
            Path(packPath.invariantSeparatorsPathString, Constants.ICON_FILENAME)
                .takeIf { it.exists() }?.toFile()?.inputStream()
        })
            ?:
            if (allowDefault)
                this::class.java.classLoader.getResourceAsStream(Constants.TAM_ICON_RESOURCE_PATH)
            else
                null
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
    fun save(progress: Reference<Double>? = null): Path {
        val packOngoingDir = Path(
            Constants.MUSIC_PACK_DIR.invariantSeparatorsPathString,
            "${Path(packName).nameWithoutExtension}.new"
        )
        val packDir = Path(
            Constants.MUSIC_PACK_DIR.invariantSeparatorsPathString,
            Path(packName).nameWithoutExtension
        )
        val assetsDir = Path(packOngoingDir.invariantSeparatorsPathString, Constants.ASSETS_DIRNAME)
        val predicatesDir = Path(
            packOngoingDir.invariantSeparatorsPathString, Constants.PREDICATES_DIRNAME)
        val rulesFile = Path(packOngoingDir.invariantSeparatorsPathString, Constants.RULES_FILENAME)
        val metaFile = Path(packOngoingDir.invariantSeparatorsPathString, Constants.META_FILENAME)
        val optionsFile = Path(
            packOngoingDir.invariantSeparatorsPathString, Constants.PACK_OPTIONS_FILENAME)
        val iconFile = Path(
            packOngoingDir.invariantSeparatorsPathString, Constants.ICON_FILENAME)
            .takeIf { it.exists() }

        val gson = GsonBuilder().setPrettyPrinting().create()
        rulesFile.toFile().writeText(gson.toJson(rules.toJson()))
        metaFile.toFile().writeText(meta.jsonEncode())
        optionsFile.toFile().writeText(options.jsonEncode())

        val outputPath = Path(packDir.invariantSeparatorsPathString + ".zip")
        val newZipPath = Path(outputPath.invariantSeparatorsPathString + ".new")

        val newZip = newZipPath.createFile()
        val assets = assetsDir.listDirectoryEntriesRecursive()
        val predicates = predicatesDir.listDirectoryEntriesRecursive()
        val totalFiles = 4 + assets.size + predicates.size
        var filesSaved = 0
        val increaseProgress = {
            filesSaved++
            progress?.value = filesSaved.toDouble() / totalFiles
        }
        FileOutputStream(newZip.pathString).use { file ->
            ZipOutputStream(file).use { out ->
                out.putNextEntry(ZipEntry(rulesFile.name))
                rulesFile.inputStream().use { it.copyTo(out) }
                increaseProgress()
                out.putNextEntry(ZipEntry(metaFile.name))
                metaFile.inputStream().use { it.copyTo(out) }
                increaseProgress()
                out.putNextEntry(ZipEntry(optionsFile.name))
                optionsFile.inputStream().use { it.copyTo(out) }
                increaseProgress()
                iconFile?.let { iconFile ->
                    out.putNextEntry(ZipEntry(iconFile.name))
                    iconFile.inputStream().use { it.copyTo(out) }
                }
                increaseProgress()

                assets.forEach { entry ->
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
                    increaseProgress()
                }

                predicates.forEach { entry ->
                    out.putNextEntry(
                        ZipEntry(
                            Path(
                                Constants.PREDICATES_DIRNAME,
                                *entry.drop(3).map { it.name }.toTypedArray()
                            ).invariantSeparatorsPathString + if (entry.isDirectory()) PATH_SEPARATOR else ""
                        )
                    )

                    entry.inputStream().use { it.copyTo(out) }
                    increaseProgress()
                }
            }
        }

        try {
            Files.move(newZip, outputPath, StandardCopyOption.REPLACE_EXISTING)
            packOngoingDir.deleteRecursively()
        }
        catch (e: Exception) {
            TAMClient.errorToast(
                Component.translatableWithFallback(
                    "trueadaptivemusic.export_failure", "Failed to export pack! Try again."),
                e.message
            )
            Logger.logError("Failed to export to zip!")
            newZip.deleteIfExists()
        }

        return outputPath
    }

    private fun performStaticValidation() {
        if (isZip) {
            ZipFile(packPath.toFile()).use { zipFile ->
                if (zipFile.entries().toList().any { it.name.contains("\\") }) {
                    validation.addWarning(
                        "This pack has not been zipped properly, likely because it is old. " +
                                "If you are the pack creator, you should re-export it before releasing it."
                    )
                }
            }
        }

        val loader = FabricLoader.getInstance()
        meta.requiredBridgeMods.forEach { mod ->
            if (!loader.isModLoaded(mod.id)) {
                validation.addWarning("This pack uses the mod ${mod.name} (${mod.id}) which could not be found.")
            }
        }

        rules.traverse { node, _ ->
            node.predicates.forEach { predicate ->
                if (predicate.arguments is ErrorPredicate.Arguments) {
                    validation.addWarning(predicate.arguments.reason)
                }
            }

            node.events.forEach { event ->
                if (event.arguments is ErrorEvent.Arguments) {
                    validation.addWarning(event.arguments.reason)
                }
            }
        }
    }

    private fun getEditPackDir(): Path {
        return Path(
            Constants.MUSIC_PACK_DIR.pathString, "${Path(packName).nameWithoutExtension}.new")
    }

    companion object {
        private val jsonErrorText =
            Component.translatableWithFallback(
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
            return MusicPack(
                MusicPackOptions(),
                MusicPackMeta(),
                MusicTree.makeEmpty(),
                "$packName.new"
            )
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
                    GsonHelper.parse(rulesFile.inputStream().reader()), playableSounds)
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
                        MusicTree.fromJson(GsonHelper.parse(it.reader()), playableSounds)
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