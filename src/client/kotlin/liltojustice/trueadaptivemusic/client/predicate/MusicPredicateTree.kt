package liltojustice.trueadaptivemusic.client.predicate

import com.google.gson.JsonObject
import net.minecraft.client.MinecraftClient
import net.minecraft.util.JsonHelper

class MusicPredicateTree private constructor(json: JsonObject) {
    private class Node(
        private val predicate: MusicPredicate,
        private val musicPath: String,
        private val children: List<Node> = listOf()) {

        fun getBottomSatisfied(client: MinecraftClient, depth: Int = 0): Pair<String, Int> {
            if (!predicate.test(client))
            {
                return Pair(musicPath, 0)
            }

            val bottoms: List<Pair<String, Int>> = List(children.size) {
                i -> children[i].getBottomSatisfied(client, depth + 1)
            }

            if (bottoms.none { bottom -> bottom.second != 0 })
            {
                return Pair(musicPath, depth)
            }

            return bottoms.maxBy { bottom -> bottom.second }
        }

        companion object {
            fun fromJson(json: JsonObject): Node {
                return Node(
                    MusicPredicate.fromJson(json),
                    JsonHelper.getString(json, "musicPath"),
                    if (JsonHelper.hasArray(json, "children"))
                        JsonHelper.getArray(json, "children").map { child -> fromJson(child.asJsonObject) }.toMutableList()
                    else listOf()
                )
            }

        }
    }

    private val root = Node.fromJson(json)

    fun getMusicToPlay(client: MinecraftClient): String {
        return root.getBottomSatisfied(client).first
    }

    companion object {
        fun fromJson(json: JsonObject): MusicPredicateTree {
            try {
                return MusicPredicateTree(json)
            } catch (e: Exception) {
                throw RulesParserException("Failed to parse rules. Inner exception:\n${e.message}")
            }
        }
    }

}