package liltojustice.trueadaptivemusic.client.trigger.predicate

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSoundFile
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.RootPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.util.JsonHelper

typealias NodeVisitor = (node: MusicPredicateTree.Node, path: List<String>) -> Unit

class MusicPredicateTree private constructor(
    json: JsonObject? = null, soundLibrary: Map<String, PlayableSoundFile> = mapOf()) {
    private val root = if (json != null) Node.fromJson(json, soundLibrary) else Node.makeRoot()

    fun toJson(): JsonObject {
        return root.toJson()
    }

    fun getMusicToPlay(client: MinecraftClient): Result {
        val result = root.getSatisfiedNode(client)
        return Result(
            result.second.joinToString(PATH_SEPARATOR),
            result.first.predicate,
            result.third.values.toList())
    }

    private fun traverseRecursive(
        root: Node,
        preorderVisitor: NodeVisitor? = null,
        postorderVisitor: NodeVisitor? = null,
        path: List<String> = emptyList()) {
        var newPath = emptyList<String>()
        try {
            newPath = path + root.predicate.getTriggerId()
        }
        catch (_: Exception) {}
        preorderVisitor?.invoke(root, newPath)
        root.forEachChild { node -> traverseRecursive(node, preorderVisitor, postorderVisitor, newPath) }
        postorderVisitor?.invoke(root, newPath)
    }

    fun traverse(preorderVisitor: NodeVisitor? = null, postorderVisitor: NodeVisitor? = null) {
        traverseRecursive(root, preorderVisitor, postorderVisitor)
    }

    fun preorderTraverse(preorderVisitor: NodeVisitor) {
        traverseRecursive(root, preorderVisitor = preorderVisitor)
    }

    companion object {
        const val PATH_SEPARATOR = "/"

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
        var events: List<MusicEvent>,
        val children: MutableList<Node> = mutableListOf()
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
            val result = predicate.toJsonFull()
            val jsonEvents = JsonArray(events.size)
            events.forEach { event -> jsonEvents.add(event.toJsonFull()) }
            val jsonChildren = JsonArray(children.size)
            children.forEach { child -> jsonChildren.add(child.toJson()) }
            result.add("events", jsonEvents)
            result.add("children", jsonChildren)

            return result
        }

        fun getSatisfiedNode(
            client: MinecraftClient, path: List<String> = emptyList(), events: Map<String, MusicEvent> = emptyMap())
                : Triple<Node, List<String>, Map<String, MusicEvent>> {
            try {
                if (!predicate.testPredicate(client)) {
                    return Triple(this, emptyList(), emptyMap())
                }
            }
            catch (e: NoClassDefFoundError) {
                Logger.logError(
                    "Testing predicate type ${predicate.getTypeName()} failed due to a class loader error. " +
                            "Are you missing a mod?\nError: $e",
                    true)

                return Triple(this, emptyList(), emptyMap())
            }
            catch (e: Exception) {
                Logger.logError(
                    "Test for predicate type ${predicate.getTypeName()} threw an exception.\nError: $e",
                    true)

                return Triple(this, emptyList(), emptyMap())
            }

            val newPath = path + predicate.getTriggerId()
            val newEvents = events + this.events.map { event -> Pair(event.getTriggerId(), event) }

            for (child in children) {
                val result = child.getSatisfiedNode(client, newPath, newEvents)

                if (result.second.isNotEmpty()) {
                    return result
                }
            }

            return Triple(this, newPath, newEvents)
        }

        fun newChild(
            predicateType: String,
            predicateParams: List<Any>,
            predicateArgs: List<Any>,
            events: List<MusicEvent>,
            playableSounds: List<PlayableSound>): Node {
            val predicate = TAMClient.predicateFactory.fromArgs(
                predicateType, playableSounds, predicateParams, predicateArgs)
            val child = Node(predicate, events)
            child.parent = this
            children.add(child)

            return child
        }

        fun isValidNewChild(child: Node): Boolean {
            return !(this === child || isChildOf(child))
        }

        fun orphan() {
            parent?.removeChild(this)
            parent = null
        }

        fun adoptChild(child: Node, position: Int? = null): Boolean {
            if (!isValidNewChild(child)) {
                return false
            }

            val adjustedPosition = position?.let {
                if (children.indexOf(child).let { index -> index != -1 && index <= it }) it - 1 else it
            }

            child.orphan()
            addChild(child, adjustedPosition)

            return true
        }

        private fun addChild(child: Node, position: Int?) {
            position?.let {
                children.add(it, child)
            } ?: children.add(child)
            child.parent = this
        }

        private fun removeChild(child: Node) {
            children.remove(child)
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
                return Node(
                    TAMClient.predicateFactory.fromJson(json, soundLibrary),
                    (json.getAsJsonArray("events") ?: JsonArray())
                        .map { element -> TAMClient.eventFactory.fromJson(element.asJsonObject, soundLibrary) },
                    parseChildren(json, soundLibrary)
                )
            }

            private fun parseChildren(json: JsonObject, soundLibrary: Map<String, PlayableSoundFile>)
                    : MutableList<Node> {
                return if (JsonHelper.hasArray(json, "children"))
                    JsonHelper.getArray(json, "children")
                        .map { child -> fromJson(child.asJsonObject, soundLibrary) }.toMutableList()
                else mutableListOf()
            }
        }
    }

    class Result(
        val path: String,
        val predicate: MusicPredicate,
        val events: List<MusicEvent>)
}