package liltojustice.trueadaptivemusic.client.predicate

import com.google.gson.JsonObject
import net.minecraft.client.MinecraftClient
import net.minecraft.util.JsonHelper

class MusicPredicateTree private constructor(json: JsonObject) {
    private class Node(
        private val predicate: MusicPredicate,
        private val musicPaths: List<String>,
        private val children: List<Node> = listOf()) {

        fun getBottomSatisfied(client: MinecraftClient, depth: Int = 0): Pair<List<String>, Int> {
            if (!predicate.test(client))
            {
                return Pair(musicPaths, 0)
            }

            val bottoms: List<Pair<List<String>, Int>> = List(children.size) {
                i -> children[i].getBottomSatisfied(client, depth + 1)
            }

            if (bottoms.none { bottom -> bottom.second != 0 })
            {
                return Pair(musicPaths, depth)
            }

            return bottoms.maxBy { bottom -> bottom.second }
        }

        companion object {
            fun fromJson(json: JsonObject): Node {
                if (!validate(json)) {
                    throw Exception("Failed to parse json node: $json")
                }

                return Node(
                    MusicPredicate.fromJson(json),
                    JsonHelper.getArray(json, "musicPaths").map { musicPath -> musicPath.asString },
                    if (JsonHelper.hasArray(json, "children"))
                        JsonHelper.getArray(json, "children").map { child -> fromJson(child.asJsonObject) }.toMutableList()
                    else listOf()
                )
            }

            private fun validate(json: JsonObject): Boolean {
                return JsonHelper.hasArray(json, "musicPaths")
            }
        }
    }

    private val root = Node.fromJson(json)

    fun getMusicToPlay(client: MinecraftClient): List<String> {
        return root.getBottomSatisfied(client).first
    }

    companion object {
        fun fromJson(json: JsonObject): MusicPredicateTree {
            return MusicPredicateTree(json)
        }
    }

}