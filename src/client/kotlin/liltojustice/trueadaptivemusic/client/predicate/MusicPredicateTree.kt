package liltojustice.trueadaptivemusic.client.predicate

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.LogLevel
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.sound.PlayableSound
import liltojustice.trueadaptivemusic.client.sound.PlayableSoundEvent
import liltojustice.trueadaptivemusic.client.sound.PlayableSoundFile
import net.minecraft.client.MinecraftClient
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import net.minecraft.util.InvalidIdentifierException
import net.minecraft.util.JsonHelper

class MusicPredicateTree private constructor(json: JsonObject, soundLibrary: Map<String, PlayableSoundFile>) {
    private class Node private constructor(
        private val predicate: MusicPredicate,
        private val playableSounds: List<PlayableSound>,
        private val children: List<Node> = listOf()) {

        fun getBottomSatisfied(client: MinecraftClient, path: List<String> = listOf()): Pair<List<PlayableSound>, List<String>> {
            if (!predicate.test(client))
            {
                return Pair(playableSounds, listOf())
            }

            val newPath = path.toMutableList()
            newPath.add(predicate.getPredicateId())

            val bottoms: List<Pair<List<PlayableSound>, List<String>>> = List(children.size) { i ->
                children[i].getBottomSatisfied(client, newPath)
            }

            if (bottoms.all { bottom -> bottom.second.isEmpty() })
            {
                return Pair(playableSounds, newPath)
            }

            return bottoms.maxBy { bottom -> bottom.second.size }
        }

        companion object {
            fun fromJson(json: JsonObject, soundLibrary: Map<String, PlayableSoundFile>): Node {
                val pred = MusicPredicate.fromJson(json)
                return Node(
                    pred,
                    parseMusicPath(json, soundLibrary),
                    parseChildren(json, soundLibrary)
                )
            }

            private fun parseMusicPath(json: JsonObject, soundLibrary: Map<String, PlayableSoundFile>)
            : List<PlayableSound> {
                 return (if (JsonHelper.hasString(json, "musicPath"))
                    listOf(JsonHelper.getString(json, "musicPath"))
                else
                    JsonHelper.getArray(json, "musicPath").map { element -> element.asString })
                     .map { path ->
                         try {
                             return@map soundLibrary[path]
                                 ?: PlayableSoundEvent(Registries.SOUND_EVENT[Identifier(path)]
                                 ?: throw InvalidIdentifierException("Couldn't find sound event for $path"))
                         } catch (_: InvalidIdentifierException) {}

                         Logger.log("Could not find \"$path\", skipping...", LogLevel.WARNING)
                         return@map null
                 }.filterNotNull()
            }

            private fun parseChildren(json: JsonObject, soundLibrary: Map<String, PlayableSoundFile>): List<Node> {
                return if (JsonHelper.hasArray(json, "children"))
                    JsonHelper.getArray(json, "children")
                        .map { child -> fromJson(child.asJsonObject, soundLibrary) }.toList()
                else listOf()
            }
        }
    }

    class Result(val playableSounds: List<PlayableSound>, val path: String)

    private val root = Node.fromJson(json, soundLibrary)

    fun getMusicToPlay(client: MinecraftClient): Result {
        val bottomSatisfied = root.getBottomSatisfied(client)
        return Result(bottomSatisfied.first, bottomSatisfied.second.joinToString("/"))
    }

    companion object {
        fun fromJson(json: JsonObject, soundLibrary: Map<String, PlayableSoundFile>): MusicPredicateTree {
            try {
                return MusicPredicateTree(json, soundLibrary)
            } catch (e: Exception) {
                throw RulesParserException("Failed to parse rules. Inner exception:\n${e.message}")
            }
        }
    }
}