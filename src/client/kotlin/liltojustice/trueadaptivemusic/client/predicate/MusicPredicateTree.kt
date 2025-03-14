package liltojustice.trueadaptivemusic.client.predicate

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.LogLevel
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.predicate.types.MusicPredicate
import liltojustice.trueadaptivemusic.client.predicate.types.RootPredicate
import liltojustice.trueadaptivemusic.client.sound.PlayableSound
import liltojustice.trueadaptivemusic.client.sound.PlayableSoundEvent
import liltojustice.trueadaptivemusic.client.sound.PlayableSoundFile
import net.minecraft.client.MinecraftClient
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import net.minecraft.util.InvalidIdentifierException
import net.minecraft.util.JsonHelper

typealias NodeVisitor = (MusicPredicateTree.Node, Int) -> Unit

class MusicPredicateTree private constructor(
    json: JsonObject? = null, soundLibrary: Map<String, PlayableSoundFile> = mapOf()) {
    private val root = if (json != null) Node.fromJson(json, soundLibrary) else Node.makeRoot()

    fun toJson(): JsonObject {
        return root.toJson()
    }

    fun getMusicToPlay(client: MinecraftClient): Result {
        val bottomSatisfied = root.getBottomSatisfied(client)
        return Result(bottomSatisfied.first, bottomSatisfied.second.joinToString("/"))
    }

    private fun traverseRecursive(
        root: Node, preorderVisitor: NodeVisitor? = null, postorderVisitor: NodeVisitor? = null, depth: Int = 0) {
        preorderVisitor?.invoke(root, depth)
        root.forEachChild { node -> traverseRecursive(node, preorderVisitor, postorderVisitor, depth + 1)}
        postorderVisitor?.invoke(root, depth)
    }

    fun traverse(preorderVisitor: NodeVisitor? = null, postorderVisitor: NodeVisitor? = null) {
        traverseRecursive(root, preorderVisitor, postorderVisitor)
    }

    fun preorderTraverse(preorderVisitor: NodeVisitor) {
        traverseRecursive(root, preorderVisitor = preorderVisitor)
    }

    fun postorderTraverse(postorderVisitor: NodeVisitor) {
        traverseRecursive(root, postorderVisitor = postorderVisitor)
    }

    companion object {
        fun makeEmpty(): MusicPredicateTree {
            return MusicPredicateTree()
        }

        fun fromJson(json: JsonObject, soundLibrary: Map<String, PlayableSoundFile>): MusicPredicateTree {
            try {
                return MusicPredicateTree(json, soundLibrary)
            } catch (e: Exception) {
                throw RulesParserException("Failed to parse rules.", e)
            }
        }
    }

    class Node private constructor(
        var predicate: MusicPredicate,
        var playableSounds: List<PlayableSound>,
        private val children: MutableList<Node> = mutableListOf()
    ) {
        var parent: Node? = null
            private set

        init {
            children.forEach { child -> child.parent = this }
        }

        fun forEachChild(visitor: (child: Node) -> Unit) {
            children.forEach(visitor)
        }

        fun toJson(): JsonObject {
            val result = predicate.toJson()
            val jsonMusicPath = JsonArray(playableSounds.size)
            playableSounds.forEach { sound -> jsonMusicPath.add(sound.getSoundName()) }
            val jsonChildren = JsonArray(children.size)
            children.forEach { child -> jsonChildren.add(child.toJson()) }
            result.add("musicPath", jsonMusicPath)
            result.add("children", jsonChildren)

            return result
        }

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

        fun newChild(predicateType: String, vararg args: Any, sounds: List<PlayableSound>) {
            val child = Node(MusicPredicate.initializeFromArgs(predicateType, *args), sounds)
            child.parent = this
            children.add(child)
        }

        fun addChild(child: Node) {
            children.add(child)
            child.parent = this
        }

        fun addChildFront(child: Node) {
            children.add(0, child)
            child.parent = this
        }

        fun removeChild(child: Node) {
            children.remove(child)
        }

        fun orphan() {
            parent?.removeChild(this)
            parent = null
        }

        fun adoptChild(child: Node): Boolean {
            if (isChildOf(child)) {
                return false
            }

            child.orphan()
            addChild(child)

            return true
        }

        fun adoptChildFront(child: Node): Boolean {
            if (isChildOf(child)) {
                return false
            }

            child.orphan()
            addChildFront(child)

            return true
        }

        private fun isChildOf(node: Node): Boolean {
            var above = parent
            while (above != null) {
                if (above == node) {
                    return true
                }

                above = above.parent
            }

            return false
        }

        companion object {
            fun makeRoot(): Node {
                return Node(RootPredicate(), listOf())
            }

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

            private fun parseChildren(json: JsonObject, soundLibrary: Map<String, PlayableSoundFile>): MutableList<Node> {
                return if (JsonHelper.hasArray(json, "children"))
                    JsonHelper.getArray(json, "children")
                        .map { child -> fromJson(child.asJsonObject, soundLibrary) }.toMutableList()
                else mutableListOf()
            }
        }
    }

    class Result(val playableSounds: List<PlayableSound>, val path: String)
}