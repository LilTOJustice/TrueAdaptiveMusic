package liltojustice.trueadaptivemusic.client.music

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.sound.file.RegularSoundFile
import liltojustice.trueadaptivemusic.client.sound.file.ZipSoundFile
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSoundEvent
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSoundFile
import liltojustice.trueadaptivemusic.client.trigger.event.ErrorEvent
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.trigger.predicate.ErrorPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicateTree
import net.minecraft.registry.Registries
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import net.minecraft.util.InvalidIdentifierException
import net.minecraft.util.JsonHelper
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.io.path.*
import kotlin.reflect.KClass

class MusicPack private constructor(
    val metadata: Metadata,
    val rules: MusicPredicateTree,
    val packName: String,
    preValidation: MusicPackValidation? = null) {
    private val packPath = Path(Constants.MUSIC_PACK_DIR, packName)
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
                ZipFile(Path(Constants.MUSIC_PACK_DIR, packWithAssets.packName).pathString).use { zipFile ->
                    zipFile.entries().toList().filter { entry -> isZipAsset(entry.name) }.forEach { entry ->
                        FileOutputStream(Path(assetsDir.pathString, Path(entry.name).name).pathString).use { out ->
                            zipFile.getInputStream(entry).use { stream -> stream.copyTo(out) }
                        }
                    }
                }
            } else if (packWithAssets != null) {
                val existingAssets = Path(Constants.MUSIC_PACK_DIR, packWithAssets.packName, Constants.ASSETS_DIRNAME)

                if (existingAssets.exists()) {
                    existingAssets.listDirectoryEntries().forEach { toCopy -> toCopy.copyTo(assetsDir) }
                }
            }
        }

        initRules()
        initMeta()

        return packDir
    }

    fun getEditPackAssetsPath(): Path {
        return Path(getEditPackDir().pathString, Constants.ASSETS_DIRNAME)
    }

    fun getEditPackAssets(): Map<String, PlayableSound> {
        return getEditPackAssetsPath().listDirectoryEntries()
            .map { file -> PlayableSoundFile(RegularSoundFile(file)) }
            .associateBy { file -> file.getSoundName() }
    }

    private fun getZipAssetNames(): List<String> {
        return ZipFile(packPath.toFile()).use { zipFile ->
            zipFile.entries().toList().filter { entry -> isZipAsset(entry.name) }.map { entry -> Path(entry.name).name }
        }
    }

    private fun getDirAssetNames(): List<String> {
        return Path(packPath.pathString, Constants.ASSETS_DIRNAME).toFile().listFiles()?.map { file -> file.name }
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

    private fun performStaticValidation() {
        var hasFFMpeg = true
        try {
            val exitCode = Runtime.getRuntime().exec(arrayOf("ffmpeg")).waitFor()
            if (exitCode != 1 && exitCode != 0) {
                hasFFMpeg = false
            }
        } catch (e: IOException) {
            hasFFMpeg = false
        }

        val nonOggFiles = getPackAssetNames().filter { name -> Path(name).extension != "ogg" }
        if (!hasFFMpeg && nonOggFiles.isNotEmpty()) {
            validation.addWarning(
                "This pack contains music that is not 'ogg' type (the only type supported by minecraft). " +
                        "This music will not play unless FFMpeg is installed on your system. See the wiki for details."
            )
        }

        val usedPredicateTypes = mutableSetOf<KClass<out MusicPredicate>>()
        val usedEventTypes = mutableSetOf<KClass<out MusicEvent>>()
        rules.traverse { node, _ ->
            (node.predicate as? ErrorPredicate)?.let {
                validation.addWarning(it.reason)
            }

            usedPredicateTypes.add(node.predicate::class)

            node.events.forEach { event ->
                (event as? ErrorEvent)?.let {
                    validation.addWarning(it.reason)
                }

                usedEventTypes.add(event::class)
            }
        }

        // TODO: Figure out how to properly include ASMDependencyAnalyzer
        /*val analyzer = ASMDependencyAnalyzer()
        usedPredicateTypes.forEach { kClass -> validateClass(kClass, analyzer) }
        usedEventTypes.forEach { kClass -> validateClass(kClass, analyzer) }*/
    }

    /*private fun validateClass(kClass: KClass<*>, analyzer: ASMDependencyAnalyzer) {
        val typeName = (kClass.companionObject?.objectInstance as? MusicPredicate.MusicPredicateCompanion<*>)
            ?.getTypeName() ?: kClass.qualifiedName
        val packageName = kClass.java.packageName
        val referencedClasses = analyzer.analyze(kClass.java.protectionDomain.codeSource.location)
        val badReferences =
            referencedClasses.filter { ref ->
                !relatedPackages(packageNameOf(ref), packageName) &&
                        runCatching { kClass.java.classLoader.loadClass(ref) }
                            .getOrDefault(false) == false }
        val commonPackages = badReferences.map { outerRef ->
            badReferences.fold(outerRef) { acc, innerRef ->
                commonPackage(acc, innerRef) ?: acc
            }
        }.toSet()
        val parentType =
            if (kClass.isSubclassOf(MusicPredicate::class))
                "Predicate "
            else if (kClass.isSubclassOf(MusicEvent::class))
                "Event "
            else
                ""

        if (badReferences.isNotEmpty()) {
            validation.addWarning(
                "$parentType$typeName references ${badReferences.size} unknown class(es) from " +
                        "${commonPackages.size} missing package(s):\n\n" + commonPackages.joinToString("\n")
                        + "\nYou are probably missing a mod.")
        }
    }*/

    private fun getEditPackDir(): Path {
        return Path(Constants.MUSIC_PACK_DIR, "${Path(packName).nameWithoutExtension}.new")
    }

    private fun isZipped(): Boolean {
        return Path(packName).extension == "zip"
    }

    companion object {
        fun loadAllPacks(): List<MusicPack> {
            return Path(Constants.MUSIC_PACK_DIR).listDirectoryEntries().mapNotNull { path ->
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
            return MusicPack(Metadata(), MusicPredicateTree.makeEmpty(), packName)
        }

        fun fromFile(filePath: Path): MusicPack {
            val zip = filePath.extension == "zip"
            if (!zip && !filePath.isDirectory()) {
                throw MusicLoadException("Given path \"$filePath\" is neither a directory nor a zip file")
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

        fun parseMusicPath(json: JsonObject, soundLibrary: Map<String, PlayableSoundFile>)
                : List<PlayableSound> {
            return (if (JsonHelper.hasString(json, "musicPath"))
                listOf(JsonHelper.getString(json, "musicPath"))
            else
                JsonHelper.getArray(json, "musicPath").map { element -> element.asString })
                .map { path ->
                    try {
                        return@map soundLibrary[path]
                            ?: PlayableSoundEvent(
                                Registries.SOUND_EVENT[Identifier(path)]
                                    ?: throw InvalidIdentifierException("Couldn't find sound event for $path")
                            )
                    } catch (_: InvalidIdentifierException) {}

                    Logger.logWarning("Could not find \"$path\", skipping...")
                    return@map null
                }.filterNotNull()
        }

        fun toPlayableSound(assets: Map<String, PlayableSound>, id: String): PlayableSound? {
            return assets[id] ?: try {
                PlayableSoundEvent(SoundEvent.of(Identifier(id)))
            }
            catch (e: InvalidIdentifierException) {
                null
            }
        }

        private fun getGson(): Gson {
            return GsonBuilder().setPrettyPrinting().create()
        }

        private fun fromDirectory(filePath: Path): MusicPack {
            val files = filePath.listDirectoryEntries()
            var metadata = Metadata()
            val assetsDir = files.find { file -> file.fileName.name == Constants.ASSETS_DIRNAME }
            if (assetsDir == null)
            {
                Logger.logInfo(
                    "Assets dir ${Constants.ASSETS_DIRNAME} is missing, so no external music will be used")
            }
            val playableSoundFiles = assetsDir?.listDirectoryEntries()
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

            val preValidation = MusicPackValidation()

            val rules = try {
                MusicPredicateTree.fromJson(
                    JsonHelper.deserialize(rulesFile.inputStream().reader()), playableSoundFiles)
            }
            catch (e: JsonParseException) {
                preValidation.addError("Could not load pack due to json error:\n$e")
                MusicPredicateTree.makeEmpty()
            }

            return MusicPack(
                metadata,
                rules,
                filePath.name,
                preValidation
            )
        }

        private fun fromZipFile(filePath: Path): MusicPack {
            ZipFile(filePath.toFile()).use { zipFile ->
                val files = zipFile.entries().toList()
                var metadata = Metadata()
                val playableSoundFiles = files
                    .filter { file -> isZipAsset(file.name) }
                    .map { file -> PlayableSoundFile(ZipSoundFile(filePath, Path(file.name))) }
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

                val preValidation = MusicPackValidation()

                val rules = try {
                    MusicPredicateTree.fromJson(
                        JsonHelper.deserialize(zipFile.getInputStream(rulesFile).reader()), playableSoundFiles)
                }
                catch (e: JsonParseException) {
                    preValidation.addError("Could not load pack due to json error:\n$e")
                    MusicPredicateTree.makeEmpty()
                }

                return MusicPack(
                    metadata,
                    rules,
                    filePath.name,
                    preValidation
                )
            }
        }

        private fun isZipAsset(fileName: String): Boolean {
            return fileName.contains(Constants.ASSETS_DIRNAME + Path("").fileSystem.separator)
        }

        private fun packageNameOf(qualifiedClassName: String): String {
            return qualifiedClassName.split(".").dropLast(1).joinToString(".")
        }

        private fun commonPackage(first: String, second: String): String? {
            return first.commonPrefixWith(second)
                .dropLastWhile { c -> c == '.' }
                .takeIf { it.split(".").size > 1 }
        }

        private fun relatedPackages(first: String, second: String): Boolean {
            return commonPackage(first, second) != null
        }
    }

    data class Metadata(var description: String = "") {
        fun toJson(): JsonObject {
            val result = JsonObject()
            result.addProperty("description", description)

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

