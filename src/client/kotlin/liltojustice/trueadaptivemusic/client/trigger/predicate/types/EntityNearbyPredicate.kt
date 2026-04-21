package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusicapi.trigger.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft

class EntityNearbyPredicate: StaticPredicateType<EntityNearbyPredicate.Arguments>("entity_nearby") {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::entities.name to "List of entities the music should play for. If none, any entity will " +
                    "trigger the music.",
            Arguments::blockRadius.name to "Minimum radius for the entity to trigger the predicate."
        )
    override val tickRate: Int
        get() = super.tickRate * 5

    data class Arguments(val entities: List<EntityTypeIdentifier>, val blockRadius: UInt): TriggerArguments()

    override fun validate(arguments: Arguments): Boolean {
        val minecraft = Minecraft.getInstance()
        val playerEntity = minecraft.player ?: return false
        val level = minecraft.level ?: return false
        val validEntities =
            (if (arguments.entities.isNotEmpty()) {
                level.entitiesForRendering()
                    .filter { entity -> arguments.entities.any { entityId -> entityId.matches(entity) } }
            }
            else {
                level.entitiesForRendering()
            })
                .filter { it != playerEntity }

        return validEntities
            .any { playerEntity.position().distanceTo(it.position()).toUInt() <= arguments.blockRadius }
    }
}