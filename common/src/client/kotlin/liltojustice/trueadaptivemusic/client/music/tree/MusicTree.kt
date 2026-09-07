package liltojustice.trueadaptivemusic.client.music.tree

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.Serialize
import liltojustice.trueadaptivemusic.client.gui.extensions.getTriggerId
import liltojustice.trueadaptivemusic.client.serialization.MusicTreeSerializer
import liltojustice.trueadaptivemusic.client.serialization.legacy.LegacyMusicTreeJsonConverter
import liltojustice.trueadaptivemusic.client.sound.SoundLibrary
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import kotlin.collections.plus
import kotlin.collections.toList

class MusicTree {
    @Serialize
    @Suppress("unused")
    private val version = LegacyMusicTreeJsonConverter.CURRENT_VERSION

    @Serialize
    private val root = MusicTreeNode.makeRoot()

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

    fun traverse(preorderVisitor: NodeVisitor? = null, postorderVisitor: NodeVisitor? = null) {
        traverseRecursive(root, preorderVisitor, postorderVisitor)
    }

    fun preorderTraverse(preorderVisitor: NodeVisitor) {
        traverseRecursive(root, preorderVisitor = preorderVisitor)
    }

    fun getNodeTitle(path: List<String>): String? {
        return getNodeTitleRecursive(root, path)?.takeIf { it.isNotBlank() }
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

        private fun traverseRecursive(
            root: MusicTreeNode,
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

        private fun getNodeTitleRecursive(root: MusicTreeNode, path: List<String>): String? {
            if (path.isEmpty()) {
                return null
            }

            if (path.size > 1) {
                val nextPathSegments = path.drop(1)
                return root.children
                    .firstNotNullOfOrNull { getNodeTitleRecursive(it, nextPathSegments) }
            }

            if (path[0] == root.getPathSegment()) {
                return root.parameters.title
            }

            return null
        }
    }

    data class Result(
        val path: String,
        val parameters: MusicTreeNode.Parameters,
        val accumulatedMusic: List<PlayableSound>,
        val accumulatedAmbience: List<PlayableSound>,
        val accumulatedEvents: List<MusicEvent<*>>,
        val parallelMusicContext: ParallelMusicContext?
    )

    data class ParallelMusicContext(val parallelMusic: List<PlayableSound>, val loopStartPoint: UInt)
}