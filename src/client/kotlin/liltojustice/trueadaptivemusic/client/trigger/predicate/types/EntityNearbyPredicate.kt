package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.util.NInt
import liltojustice.trueadaptivemusicapi.identifier.EntityIdentifier
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.MinecraftClient
import kotlin.reflect.typeOf

object EntityNearbyPredicate: StaticPredicateType<EntityNearbyPredicate.Arguments>(
    "entity_nearby", typeOf<Arguments>()
) {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::entities.name to "List of entities the music should play for. If none, any entity will " +
                    "trigger the music.",
            Arguments::blockRadius.name to "Minimum radius for the entity to trigger the predicate.",
            Arguments::minimumCount.name to "Select how many minimum mobs it takes to trigger the music."
        )
    override val tickRate: Int
        get() = super.tickRate * 5

    data class Arguments(
        val entities: List<EntityIdentifier> = emptyList(),
        val blockRadius: UInt = 0U,
        val minimumCount: NInt = NInt()
    ): TriggerArguments()

    override fun test(arguments: Arguments): Boolean {
        val minecraft = MinecraftClient.getInstance()
        val playerEntity = minecraft.player ?: return false
        val level = minecraft.world ?: return false
        val validEntities =
            (if (arguments.entities.isNotEmpty()) {
                level.entities.filter { entity -> arguments.entities.any { entityId -> entityId.matches(entity) } }
            }
            else {
                level.entities
            })
                .filter { it != playerEntity }

        return validEntities
            .count {
                playerEntity.position().distanceTo(it.position()).toUInt() <= arguments.blockRadius
            } >= arguments.minimumCount.toInt()
    }
}