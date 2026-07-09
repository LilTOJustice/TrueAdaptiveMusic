package liltojustice.trueadaptivemusic.common.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.common.client.network.TAMClientNetworking
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft
import kotlin.reflect.typeOf

object SpawnPointNearbyPredicate: StaticPredicateType<SpawnPointNearbyPredicate.Arguments>(
    "spawn_point_nearby", typeOf<Arguments>()
) {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::blockRadius.name to "Maximum distance from the current spawn point in which the music will play"
        )

    data class Arguments(val blockRadius: UInt): TriggerArguments()

    override fun test(arguments: Arguments): Boolean {
        val player = Minecraft.getInstance().player ?: return false
        val spawnPoint = TAMClientNetworking.spawnPoint ?: return false
        val blockRadiusSquared = arguments.blockRadius.toDouble().let { it * it }
        return spawnPoint.dimension() == player.level().dimension() &&
                spawnPoint.pos().distSqr(player.blockPosition()) <= blockRadiusSquared
    }
}