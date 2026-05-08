package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.identifier.BlockIdentifier
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import kotlin.reflect.typeOf

object BlockNearbyPredicate: StaticPredicateType<BlockNearbyPredicate.Arguments>(
    "block_nearby", typeOf<Arguments>()
) {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::blocks.name to "List of block the music should play for. If none, any block will trigger the " +
                    "music.",
            Arguments::blockDistance.name to "Minimum distance (axis-dependent) for the block to trigger the predicate."
        )
    override val tickRate: Int
        get() = super.tickRate * 10

    data class Arguments(val blocks: List<BlockIdentifier>, val blockDistance: UInt): TriggerArguments()

    override fun test(arguments: Arguments): Boolean {
        val minecraft = MinecraftClient.getInstance()
        val playerEntity = minecraft.player ?: return false
        val level = minecraft.world ?: return false

        if (arguments.blocks.isEmpty()) {
            return true
        }

        val distance = arguments.blockDistance.toDouble()
        val vec = Vec3d(distance, distance, distance)
        val aabb = Box(playerEntity.entityPos.subtract(vec), playerEntity.entityPos.add(vec))

        return level.getStatesInBox(aabb).anyMatch { blockState ->
            arguments.blocks.any { blockId -> blockId.matches(blockState) }
        }
    }
}