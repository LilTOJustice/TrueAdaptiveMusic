package liltojustice.trueadaptivemusic.client.predicate

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.LogLevel
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.PlayableSound
import liltojustice.trueadaptivemusic.client.PlayableSoundEvent
import liltojustice.trueadaptivemusic.client.PlayableSoundFile
import net.minecraft.client.MinecraftClient
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import net.minecraft.util.InvalidIdentifierException
import net.minecraft.util.JsonHelper
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

class MusicPredicateTree private constructor(json: JsonObject, packName: String) {
    private class Node(
        private val predicate: MusicPredicate,
        private val playableSounds: List<PlayableSound>,
        private val children: List<Node> = listOf()) {

        fun getBottomSatisfied(client: MinecraftClient, depth: Int = 0): Pair<List<PlayableSound>, Int> {
            if (!predicate.test(client))
            {
                return Pair(playableSounds, 0)
            }

            val bottoms: List<Pair<List<PlayableSound>, Int>> = List(children.size) {
                i -> children[i].getBottomSatisfied(client, depth + 1)
            }

            if (bottoms.none { bottom -> bottom.second != 0 })
            {
                return Pair(playableSounds, depth)
            }

            return bottoms.maxBy { bottom -> bottom.second }
        }

        companion object {
            fun fromJson(json: JsonObject, packName: String, predicatePath: String = ""): Node {
                val pred = MusicPredicate.fromJson(json, predicatePath)
                return Node(
                    pred,
                    parseMusicPath(json, packName, pred.getPredicatePath()),
                    parseChildren(json, packName, pred.getPredicatePath())
                )
            }

            private fun parseMusicPath(json: JsonObject, packName: String, predicatePath: String): List<PlayableSound> {
                return expandDirectories((if (JsonHelper.hasString(json, "musicPath"))
                    listOf(JsonHelper.getString(json, "musicPath"))
                else
                    JsonHelper.getArray(json, "musicPath").map { element -> element.asString }), packName, predicatePath)
            }

            private fun expandDirectories(paths: List<String>, packName: String, predicatePath: String): List<PlayableSound> {
                val expanded: MutableList<PlayableSound> = mutableListOf()
                paths.forEach { pathName ->
                    var fullPath: Path? = null
                    var identifier: Identifier? = null
                    try {
                        fullPath = Path("${Constants.MUSIC_PACK_DIR}/$packName/$pathName")
                    } catch (_: InvalidPathException) {}
                    try {
                        identifier = Identifier(pathName)
                    } catch (_: InvalidIdentifierException) {}

                    if (fullPath?.isDirectory() == true) {
                        expanded.addAll(fullPath.toFile().listFiles()
                            ?.map { file -> PlayableSoundFile(Path("$fullPath/${file.name}"), predicatePath) }
                            ?: listOf())
                    } else if (fullPath?.exists() == true){
                        expanded.add(PlayableSoundFile(fullPath, predicatePath))
                    } else if (Registries.SOUND_EVENT.containsId(identifier)) {
                        expanded.add(PlayableSoundEvent(Registries.SOUND_EVENT[Identifier(pathName)]!!, predicatePath))
                    } else {
                        Logger.log("Could not find proper path for $fullPath, skipping...", LogLevel.WARNING)
                    }
                }

                return expanded
            }

            private fun parseChildren(json: JsonObject, packName: String, predicatePath: String): List<Node> {
                return if (JsonHelper.hasArray(json, "children"))
                    JsonHelper.getArray(json, "children")
                        .map { child -> fromJson(child.asJsonObject, packName, predicatePath) }.toList()
                else listOf()
            }
        }
    }

    private val root = Node.fromJson(json, packName)

    fun getMusicToPlay(client: MinecraftClient): List<PlayableSound> {
        return root.getBottomSatisfied(client).first
    }

    companion object {
        fun fromJson(json: JsonObject, packName: String): MusicPredicateTree {
            try {
                return MusicPredicateTree(json, packName)
            } catch (e: Exception) {
                throw RulesParserException("Failed to parse rules. Inner exception:\n${e.message}")
            }
        }
    }

}