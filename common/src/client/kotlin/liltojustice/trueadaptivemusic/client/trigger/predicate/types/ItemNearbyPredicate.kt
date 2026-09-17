package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.util.NInt
import liltojustice.trueadaptivemusicapi.identifier.ItemIdentifier
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.item.ItemEntity
import kotlin.reflect.typeOf

object ItemNearbyPredicate: StaticPredicateType<ItemNearbyPredicate.Arguments>(
    "item_nearby", typeOf<Arguments>()
) {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::items.name to "List of items the music should play for. If none, any item will trigger " +
                    "the music.",
            Arguments::blockRadius.name to "Minimum radius for the item to trigger the predicate.",
            Arguments::minimumCount.name to "Select how many minimum items it takes to trigger the music.",
            Arguments::includeInventory.name to "Whether to include the player's inventory as \"nearby\"."
        )
    override val tickRate: Int
        get() = super.tickRate * 5

    data class Arguments(
        val items: List<ItemIdentifier> = emptyList(),
        val blockRadius: UInt = 0U,
        val minimumCount: NInt = NInt(),
        val includeInventory: Boolean = false
    ): TriggerArguments()

    override fun test(arguments: Arguments): Boolean {
        val minecraft = Minecraft.getInstance()
        val playerEntity = minecraft.player ?: return false
        val level = minecraft.level ?: return false
        if (arguments.includeInventory &&
            playerEntity.inventory.contains { invItem ->
                arguments.items.any { it.matches(invItem.typeHolder()) } }) {
            return true
        }

        val validEntities =
            (if (arguments.items.isNotEmpty()) {
                level.entitiesForRendering()
                    .filterIsInstance<ItemEntity>()
                    .filter { entity ->
                        arguments.items.any { entityId -> entityId.matches(entity.item.typeHolder()) }
                    }
            }
            else {
                level.entitiesForRendering().filterIsInstance<ItemEntity>()
            })
                .filter { it != playerEntity }

        return validEntities
            .count {
                playerEntity.position().distanceTo(it.position()).toUInt() <= arguments.blockRadius
            } >= arguments.minimumCount.toInt()
    }
}