package liltojustice.trueadaptivemusic.client.music.tree

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.Serialize
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.extensions.getTriggerId
import liltojustice.trueadaptivemusic.client.music.pack.MusicPackOptions
import liltojustice.trueadaptivemusic.client.serialization.MusicTreeSerializer
import liltojustice.trueadaptivemusic.client.serialization.legacy.LegacyMusicTreeJsonConverter
import liltojustice.trueadaptivemusic.client.sound.SoundLibrary
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.trigger.MusicParameters
import liltojustice.trueadaptivemusic.client.trigger.MusicTriggerException
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.RootPredicate
import liltojustice.trueadaptivemusic.client.util.NInt
import liltojustice.trueadaptivemusic.client.util.TimeOfDay
import liltojustice.trueadaptivemusic.text.StringExtensions.prettify
import liltojustice.trueadaptivemusic.text.translatableWithFallbackOrNull
import liltojustice.trueadaptivemusicapi.TAMAPI
import liltojustice.trueadaptivemusicapi.trigger.arguments.EmptyTriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.state.EmptyTriggerState
import net.minecraft.network.chat.Component
import kotlin.collections.emptyMap
import kotlin.collections.plus
import kotlin.collections.toList
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.primaryConstructor

typealias NodeVisitor = (node: MusicTree.Node, path: List<String>) -> Unit

class MusicTree {
    @Serialize
    @Suppress("unused")
    private val version = LegacyMusicTreeJsonConverter.CURRENT_VERSION

    @Serialize
    private val root = Node.makeRoot()

    fun toJson(): JsonObject {
        return MusicTreeSerializer.serialize(this)
    }

    fun getMusicToPlay(): Result {
        val result = root.getSatisfiedNode()
            ?: return Result(
                Constants.ROOT_PREDICATE_NAME,
                root.parameters,
                root.music,
                root.ambience,
                root.events,
                root.music
                    .takeIf { root.parameters.parallelMusic }
                    ?.let { music ->
                        ParallelMusicContext(
                            music + root.getMusicRecursive(),
                            root.parameters.loopStartPoints.values.firstOrNull() ?: 0U
                        )
                    }
            )
        val parallelMusic = result.music
            .takeIf { result.node.parameters.parallelMusic }
            ?.let { music ->
                result.parallelRoot?.let { parallelRoot ->
                    ParallelMusicContext(
                        music + result.node.getMusicRecursive(),
                        parallelRoot.parameters.loopStartPoints.values.firstOrNull() ?: 0U
                    )
                }
            }

        return Result(
            result.path.joinToString(PATH_SEPARATOR),
            result.node.parameters,
            result.music,
            result.ambience,
            result.events.values.toList(),
            parallelMusic,
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
        var predicates: MutableList<MusicPredicate<*>>,
        var events: List<MusicEvent<*>>,
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
            path: List<String> = emptyList(),
            eventCollection: Map<String, MusicEvent<*>> = emptyMap(),
            musicCollection: Set<PlayableSound> = emptySet(),
            ambienceCollection: Set<PlayableSound> = emptySet(),
            parallelRoot: Node? = null
        ): Result? {
            val parallelRoot = this.takeIf { parallelRoot == null } ?: parallelRoot
            predicates.forEach { predicate ->
                try {
                    val last = predicate.lastResult
                    if (!predicate.test()) {
                        if (last) {
                            parent?.predicates?.forEach { it.resetCache() }
                        }

                        return@forEach
                    }
                    else if (!last) {
                        children.forEach { child -> child.predicates.forEach { it.resetCache() } }
                    }
                }
                catch (e: NoClassDefFoundError) {
                    Logger.logError(
                        "Testing predicates failed due to a class loader error. " +
                                "Are you missing a mod?\nError: $e",
                        true
                    )

                    return null
                }
                catch (e: Exception) {
                    Logger.logError("Testing predicates threw an exception.\nError: $e", true)

                    return null
                }

                val newPath = path + predicates.joinToString(", ") { it.getTriggerId() }
                val newEvents = eventCollection +
                        this.events.map { event -> Pair(event.getTriggerId(), event) }
                val newMusic = this.music.toSet() +
                        if (parameters.inheritMusic || parent?.parameters?.parallelMusic == true)
                            musicCollection
                        else
                            emptySet()
                val newAmbience = ambience.toSet() +
                        if (parameters.inheritAmbience)
                            ambienceCollection
                        else
                            emptySet()

                for (child in children) {
                    child.getSatisfiedNode(
                        newPath,
                        newEvents,
                        newMusic,
                        newAmbience,
                        parallelRoot
                    )?.let { return it }
                }

                return if (parameters.requireChildren) {
                    null
                } else {
                    Result(
                        this,
                        newPath,
                        newEvents,
                        newMusic.toList(),
                        newAmbience.toList(),
                        parallelRoot
                    )
                }
            }

            return null
        }

        fun newChild(
            parameters: List<Any>,
            events: List<MusicEvent<*>>,
            music: List<PlayableSound>,
            ambience: List<PlayableSound>
        ): Node {
            val child = Node(music, ambience, mutableListOf(), events, Parameters.fromArgs(parameters))
            child.parent = this
            children.add(child)

            return child
        }

        fun newPredicate(predicateTypeName: String, predicateArgs: List<Any?>): Node {
            val predicateType = TAMAPI.getPredicateType(predicateTypeName)
                ?: throw MusicTriggerException("Unknown predicate type '$predicateTypeName'")
            val predicate = TAMClient.musicPredicateFactory.fromArgs(
                predicateType, TAMAPI.makePredicateArguments(predicateType, predicateArgs))
            predicates.add(predicate)

            return this
        }

        fun copy(withChildren: Boolean): Node {
            return Node(
                music,
                ambience,
                predicates.map { TAMClient.musicPredicateFactory.fromArgs(it.type, it.arguments) }.toMutableList(),
                events.map { TAMClient.musicEventFactory.makeCopy(it) },
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

        fun getMusicRecursive(): Set<PlayableSound> {
            return music.toSet() + children.flatMap { it.getMusicRecursive() }.toSet()
        }

        companion object {
            fun makeRoot(): Node {
                return Node(
                    listOf(),
                    listOf(),
                    mutableListOf(MusicPredicate(
                        RootPredicate, EmptyTriggerArguments(), EmptyTriggerState())),
                    listOf(),
                    Parameters.default()
                )
            }
        }

        class Result(
            val node: Node,
            val path: List<String>,
            val events: Map<String, MusicEvent<*>>,
            val music: List<PlayableSound>,
            val ambience: List<PlayableSound>,
            val parallelRoot: Node?
        )

        data class Parameters(
            var musicWeights: Map<String, NInt> = mapOf(),
            var vanillaMusic: Boolean = false,
            var compatibilityMode: Boolean = false,
            var disableFading: Boolean = false,
            var disableResuming: Boolean = false,
            var ignorePersistence: Boolean = false,
            var trackDelay: UInt = 0U,
            var trackDelayNoise: UInt = 0U,
            var enterDelay: UInt = 0U,
            var inheritMusic: Boolean = false,
            var inheritAmbience: Boolean = true,
            var parallelMusic: Boolean = false,
            var requireChildren: Boolean = false,
            var loopMusic: Boolean = false,
            var loopStartPoints: Map<String, UInt> = mapOf()
        ): MusicParameters() {
            companion object: ParametersCompanion<Parameters> {
                override val displayNames: Map<String, String>
                    get() = super.displayNames +
                            Parameters::class.declaredMembers.map { it.name }.associateWith { it.prettify() }

                override val descriptions: Map<String, String>
                    get() = super.descriptions + mapOf(
                        Parameters::musicWeights.name to "Change these values to adjust how likely different tracks " +
                                "are to play. The higher the value, the more often they will play relative to others.",
                        Parameters::vanillaMusic.name to "If checked, TAM will use vanilla music when this node is " +
                                "selected.\n\nUse this if you want music to fallback to vanilla in this node, (i.e. " +
                                "you want mod-specific music to play).\n\nCertain music-related parameters can't be " +
                                "used with this enabled.",
                        Parameters::compatibilityMode.name to "If checked with " +
                                "\"${getParamDisplayName(Parameters::vanillaMusic.name)!!.string}\", " +
                                "fully disables TAM to allow other music to play. Great for if a mod has specific " +
                                "music with complicated logic that TAM doesn't account for.",
                        Parameters::disableFading.name to "If checked, music that is already playing will " +
                                "immediately stop rather than fading out when entering this node.",
                        Parameters::disableResuming.name to "If checked, music will not resume when returning to " +
                                "this node.",
                        Parameters::ignorePersistence.name to "\"${MusicPackOptions.getArgDisplayName(
                            MusicPackOptions::persistentNodeMusic.name)!!.string}\" pack option will be " +
                                "ignored when this node is selected. Music for this node will start playing right " +
                                "when it is selected, and the music in this node will not persist.",
                        Parameters::trackDelay.name to "After a track finishes, wait this many seconds before " +
                                "playing the next.",
                        Parameters::trackDelayNoise.name to "Add randomly + or - this many seconds to track delay.",
                        Parameters::enterDelay.name to "Wait this many seconds before starting music when entering " +
                                "this predicate. Disables music resuming for this predicate.",
                        Parameters::inheritMusic.name to "Include this nodes's parent's music instead of overriding " +
                                "it.",
                        Parameters::inheritAmbience.name to "Include this nodes's parent's ambience instead of " +
                                "overriding it.",
                        Parameters::parallelMusic.name to "Allow music across nodes to be played in parallel and " +
                                "transition between music as the active node changes.\n\nSelecting this makes all " +
                                "descendants automatically have this checked to participate in the parallelism.\n\n" +
                                "Only one track is allowed per node with this property.\n\nMusic inheritance and " +
                                "delays are disabled, and looping is forced on.",
                        Parameters::requireChildren.name to "If checked, this node will be ignored unless one of " +
                                "its immediate children can also be activated.",
                        Parameters::loopMusic.name to "A random selected track is picked once, and then looped " +
                                "forever until the node is left.\n\n* Disables ${MusicPackOptions.getArgDisplayName(
                                    MusicPackOptions::persistentNodeMusic.name)!!.string} for this node.",
                        Parameters::loopStartPoints.name to "Some looping music has an intro before the loop starts." +
                                "\n\nThis denotes, for each looping track, where the intro ends and the loop starts." +
                                "\n\nGive a value in milliseconds from the start. Leave this as 0 if there is no " +
                                "intro."
                    )
                private val json = GsonBuilder()
                    .registerTypeAdapter(NInt::class.java, NInt.NIntTypeAdapter)
                    .registerTypeAdapter(TimeOfDay::class.java, TimeOfDay.TimeOfDayTypeAdapter)
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

                fun getParamDisplayName(paramName: String): Component? {
                    return translatableWithFallbackOrNull(
                        "trueadaptivemusic.param.node.${paramName}.display", displayNames[paramName])
                }

                fun getParamDescription(paramName: String): Component {
                    return Component.translatableWithFallback(
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
        val accumulatedEvents: List<MusicEvent<*>>,
        val parallelMusicContext: ParallelMusicContext?
    )

    data class ParallelMusicContext(val parallelMusic: List<PlayableSound>, val loopStartPoint: UInt)
}