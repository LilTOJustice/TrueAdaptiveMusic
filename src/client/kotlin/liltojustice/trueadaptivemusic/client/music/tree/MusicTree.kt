package liltojustice.trueadaptivemusic.client.music.tree

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.Serialize
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.serialization.MusicTreeSerializer
import liltojustice.trueadaptivemusic.client.sound.SoundLibrary
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.RootPredicate
import liltojustice.trueadaptivemusic.text.StringExtensions.prettify
import liltojustice.trueadaptivemusic.text.translatableWithFallbackOrNull
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import kotlin.collections.plus
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.primaryConstructor

typealias NodeVisitor = (node: MusicTree.Node, path: List<String>) -> Unit

class MusicTree {
    @Serialize
    @Suppress("unused")
    private val version = SERIALIZATION_VERSION

    @Serialize
    private val root = Node.makeRoot()

    fun toJson(): JsonObject {
        return MusicTreeSerializer.serialize(this)
    }

    fun getMusicToPlay(client: MinecraftClient): Result {
        val result = root.getSatisfiedNode(client)
        return Result(
            result.path.joinToString(PATH_SEPARATOR),
            result.node.parameters,
            result.music,
            result.ambience,
            result.events.values.toList()
        )
    }

    fun initializeParents() {
        root.initializeParents()
    }

    private fun traverseRecursive(
        root: Node,
        preorderVisitor: NodeVisitor? = null,
        postorderVisitor: NodeVisitor? = null,
        path: List<String> = emptyList()) {
        var newPath = emptyList<String>()
        try {
            newPath = path + root.predicates.joinToString { it.getTriggerId() }
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
        const val SERIALIZATION_VERSION = 2
        const val PATH_SEPARATOR = "/"

        fun makeEmpty(): MusicTree {
            return MusicTree()
        }

        fun fromJson(json: JsonObject, soundLibrary: SoundLibrary): MusicTree {
            try {
                return MusicTreeSerializer.deserialize(json, soundLibrary)
            } catch (e: Exception) {
                throw RulesParserException("Failed to parse rules.", e)
            }
        }
    }

    class Node private constructor(
        var music: List<PlayableSound>,
        var ambience: List<PlayableSound>,
        var predicates: MutableList<MusicPredicate>,
        var events: List<MusicEvent>,
        var parameters: Parameters,
        val children: MutableList<Node> = mutableListOf()
    ) {
        var parent: Node? = null
            private set

        init {
            initializeParents()
        }

        fun initializeParents() {
            children.forEach { child ->
                child.parent = this
                child.initializeParents()
            }
        }

        fun forEachChild(visitor: (child: Node) -> Unit) {
            children.forEach(visitor)
        }

        fun getSatisfiedNode(
            client: MinecraftClient,
            path: List<String> = emptyList(),
            eventCollection: Map<String, MusicEvent> = emptyMap(),
            musicCollection: Set<PlayableSound> = emptySet(),
            ambienceCollection: Set<PlayableSound> = emptySet()): Result {
            predicates.forEach { predicate ->
                try {
                    if (!predicate.testPredicate()) {
                        return@forEach
                    }
                }
                catch (e: NoClassDefFoundError) {
                    Logger.logError(
                        "Testing predicates failed due to a class loader error. " +
                                "Are you missing a mod?\nError: $e",
                        true)

                    return Result(
                        this, emptyList(), emptyMap(), emptyList(), emptyList())
                }
                catch (e: Exception) {
                    Logger.logError("Testing predicates threw an exception.\nError: $e", true)

                    return Result(
                        this, emptyList(), emptyMap(), emptyList(), emptyList())
                }

                val newPath = path + predicate.getTriggerId()
                val newEvents = eventCollection + this.events.map { event -> Pair(event.getTriggerId(), event) }
                val newMusic = this.music.toSet() +
                        if (parameters.inheritMusic)
                            musicCollection
                        else
                            emptySet()
                val newAmbience = ambience.toSet() +
                        if (parameters.inheritAmbience)
                            ambienceCollection
                        else
                            emptySet()

                for (child in children) {
                    val result = child.getSatisfiedNode(client, newPath, newEvents, newMusic, newAmbience)

                    if (result.path.isNotEmpty()) {
                        return result
                    }
                }

                return Result(this, newPath, newEvents, newMusic.toList(), newAmbience.toList())
            }

            return Result(this, emptyList(), emptyMap(), emptyList(), emptyList())
        }

        fun newChild(
            parameters: List<Any>,
            events: List<MusicEvent>,
            music: List<PlayableSound>,
            ambience: List<PlayableSound>): Node {
            val child = Node(music, ambience, mutableListOf(), events, Parameters.fromArgs(parameters))
            child.parent = this
            children.add(child)

            return child
        }

        fun newPredicate(predicateType: String, predicateArgs: List<Any>): Node {
            val predicate = TAMClient.predicateFactory.fromArgs(predicateType, predicateArgs)
            predicates.add(predicate)

            return this
        }

        fun copy(withChildren: Boolean): Node {
            return Node(
                music,
                ambience,
                predicates.map { TAMClient.predicateFactory.makeCopy(it) }.toMutableList(),
                events.map { TAMClient.eventFactory.makeCopy(it) },
                parameters.copy(),
                if (withChildren)
                    children.map { it.copy(true) }.toMutableList()
                else
                    mutableListOf()
            )
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
                return Node(
                    listOf(),
                    listOf(),
                    mutableListOf(RootPredicate()),
                    listOf(),
                    Parameters.default()
                )
            }
        }

        class Result(
            val node: Node,
            val path: List<String>,
            val events: Map<String, MusicEvent>,
            val music: List<PlayableSound>,
            val ambience: List<PlayableSound>
        )

        data class Parameters(
            var trackDelay: UInt = 0U,
            var trackDelayNoise: UInt = 0U,
            var enterDelay: UInt = 0U,
            var inheritMusic: Boolean = false,
            var inheritAmbience: Boolean = true,
            var loopMusic: Boolean = false,
            var loopStartPoints: Map<String, UInt> = mapOf()
        ): MusicTrigger.Parameters() {
            companion object: ParametersCompanion<Parameters> {
                override val displayNames: Map<String, String>
                    get() = super.displayNames +
                            Parameters::class.declaredMembers.map { it.name }.associateWith { it.prettify() }

                override val descriptions: Map<String, String>
                    get() = super.descriptions + mapOf(
                        "trackDelay" to "After a track finishes, wait this many seconds before playing the next.",
                        "trackDelayNoise" to "Add randomly + or - this many seconds to track delay.",
                        "enterDelay" to "Wait this many seconds before starting music when entering this predicate. " +
                                "Disables music resuming for this predicate.",
                        "inheritMusic" to "Include this predicate's parent's music along with this predicate's music.",
                        "inheritAmbience" to "Include this predicate's parent's ambience along with this predicate's " +
                                "ambience.",
                        "loopMusic" to "A random selected track is picked once, and then looped forever until the" +
                                " node is left.",
                        "loopStartPoints" to "Some looping music has an intro before the loop starts.\n\nThis " +
                                "denotes, for each looping track, where the intro ends and the loop starts." +
                                "\n\nGive a value in milliseconds from the start. Leave this as 0 if " +
                                "there is no intro.\n\n* Disables persistent node music for this node."
                    )
                private val json = GsonBuilder()
                    .setPrettyPrinting()
                    .create()


                override fun default(): Parameters {
                    return Parameters()
                }

                fun jsonDecode(input: JsonObject): Parameters {
                    return json.fromJson(input, Parameters::class.java)
                }

                fun fromArgs(args: List<Any>): Parameters {
                    return Parameters::class.primaryConstructor?.call(*args.toTypedArray()) ?: default()
                }

                fun getParamDisplayName(paramName: String): Text? {
                    return translatableWithFallbackOrNull(
                        "trueadaptivemusic.param.node.${paramName}.display", displayNames[paramName])
                }

                fun getParamDescription(paramName: String): Text? {
                    return Text.translatableWithFallback(
                        "trueadaptivemusic.param.node.${paramName}.description", descriptions[paramName])
                }
            }
        }
    }

    data class Result(
        val path: String,
        val parameters: Node.Parameters,
        val accumulatedMusic: List<PlayableSound>,
        val accumulatedAmbience: List<PlayableSound>,
        val accumulatedEvents: List<MusicEvent>
    )
}