package liltojustice.trueadaptivemusic.client.predicate

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.MusicPack
import liltojustice.trueadaptivemusic.client.event.MusicEvent
import liltojustice.trueadaptivemusic.client.predicate.types.RootPredicate
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSoundFile
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import net.minecraft.client.MinecraftClient
import net.minecraft.util.JsonHelper
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

typealias NodeVisitor = (MusicPredicateTree.Node, Int) -> Unit

class MusicPredicateTree private constructor(
    json: JsonObject? = null, soundLibrary: Map<String, PlayableSoundFile> = mapOf()) {
    private val root = if (json != null) Node.fromJson(json, soundLibrary) else Node.makeRoot()

    fun toJson(): JsonObject {
        return root.toJson()
    }

    fun getMusicToPlay(client: MinecraftClient): Result {
        val result = root.getSatisfiedNode(client)
        return Result(
            result.second.joinToString("/"),
            result.first.playableSounds,
            result.first.parameters,
            result.third.values.toList())
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
        var events: List<MusicEvent>,
        var parameters: Parameters = Parameters(),
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
            val jsonEvents = JsonArray(events.size)
            events.forEach { event -> jsonEvents.add(event.toJson()) }
            result.add("musicPath", jsonMusicPath)
            result.add("events", jsonEvents)
            result.add("parameters", parameters.toJson())
            result.add("children", jsonChildren)

            return result
        }

        fun getSatisfiedNode(
            client: MinecraftClient, path: List<String> = emptyList(), events: Map<String, MusicEvent> = emptyMap())
        : Triple<Node, List<String>, Map<String, MusicEvent>> {
            if (!predicate.test(client)) {
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
            nodeArgs: List<Any>,
            predicateArgs: List<Any>,
            events: List<MusicEvent>,
            sounds: List<PlayableSound>) {
            val child = Node(
                MusicPredicate.initializeFromArgs(predicateType, *predicateArgs.toTypedArray()),
                sounds,
                events,
                Parameters.initializeFromArgs(*nodeArgs.toTypedArray()))
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
                return Node(RootPredicate(), listOf(), listOf())
            }

            fun fromJson(json: JsonObject, soundLibrary: Map<String, PlayableSoundFile>): Node {
                return Node(
                    MusicPredicate.fromJson(json),
                    MusicPack.parseMusicPath(json, soundLibrary),
                    MusicEvent.arrayFromJsonArray(
                        json.getAsJsonArray("events") ?: JsonArray(), soundLibrary),
                    json.getAsJsonObject("parameters")?.let { Parameters.fromJson(it) }
                        ?: Parameters(),
                    parseChildren(json, soundLibrary)
                )
            }

            private fun parseChildren(json: JsonObject, soundLibrary: Map<String, PlayableSoundFile>): MutableList<Node> {
                return if (JsonHelper.hasArray(json, "children"))
                    JsonHelper.getArray(json, "children")
                        .map { child -> fromJson(child.asJsonObject, soundLibrary) }.toMutableList()
                else mutableListOf()
            }
        }

        data class Parameters(val trackDelay: UInt = 0U, val trackDelayNoise: UInt = 0U) {
            fun toJson(): JsonObject {
                val result = JsonObject()
                result.addProperty("trackDelay", trackDelay.toInt())
                result.addProperty("trackDelayNoise", trackDelayNoise.toInt())

                return result
            }

            fun constructorParams(): List<Any?> {
                return this::class.declaredMemberProperties
                    .filter { property ->
                        this::class.primaryConstructor!!.parameters.any { param -> property.name == param.name } }
                    .map { property ->
                        property.getter.call(this)
                    }
            }

            companion object {
                fun initializeFromArgs(vararg constructorArgs: Any): Parameters {
                    return Parameters::class.primaryConstructor?.call(*constructorArgs) ?: Parameters()
                }

                fun fromJson(json: JsonObject): Parameters {
                    return Parameters(
                        json.getAsJsonPrimitive("trackDelay")?.asInt?.toUInt() ?: 0U,
                        json.getAsJsonPrimitive("trackDelayNoise")?.asInt?.toUInt() ?: 0U)
                }
            }
        }
    }

    class Result(
        val path: String,
        val playableSounds: List<PlayableSound>,
        val parameters: Node.Parameters,
        val events: List<MusicEvent>)
}