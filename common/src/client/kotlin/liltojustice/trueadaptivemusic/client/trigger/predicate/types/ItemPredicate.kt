package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.identifier.ItemIdentifier
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Player
import kotlin.reflect.typeOf

object ItemPredicate: StaticPredicateType<ItemPredicate.Arguments>(
    "item", typeOf<Arguments>()
) {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::items.name to "Select all items the music should play for.",
            Arguments::location.name to "Select whether the music should play if the item is in the player's " +
                    "inventory, hotbar, either hand, or a specific hand."
        )

    override val tickRate: Int
        get() = super.tickRate * 3

    override fun test(arguments: Arguments): Boolean {
        val minecraft = Minecraft.getInstance()
        val player = minecraft.player ?: return false

        return arguments.items.any { playerHas(player, it, arguments.location) }
    }

    data class Arguments(val items: List<ItemIdentifier>, val location: Location): TriggerArguments()

    enum class Location {
        Inventory,
        Hotbar,
        Hand,
        MainHand,
        Offhand
    }

    private fun playerHas(player: Player, item: ItemIdentifier, location: Location): Boolean {
        return when(location) {
            Location.Inventory -> playerHasInInventory(player, item)
            Location.Hotbar -> playerHasInHotbar(player, item)
            Location.Hand -> playerHasInHand(player, item)
            Location.MainHand -> playerHasInMainHand(player, item)
            Location.Offhand -> playerHasInOffhand(player, item)
        }
    }

    private fun playerHasInInventory(player: Player, item: ItemIdentifier): Boolean {
        return player.inventory.contains { item.matches(it.itemHolder) }
    }

    private fun playerHasInHotbar(player: Player, item: ItemIdentifier): Boolean {
        for (i in 0..8) {
            player.inventory.getSlot(i)?.takeIf { item.matches(it.get().itemHolder) }?.run { return true }
        }

        return false
    }

    private fun playerHasInHand(player: Player, item: ItemIdentifier): Boolean {
        return playerHasInMainHand(player, item) || playerHasInOffhand(player, item)
    }

    private fun playerHasInMainHand(player: Player, item: ItemIdentifier): Boolean {
        return item.matches(player.mainHandItem.itemHolder)
    }

    private fun playerHasInOffhand(player: Player, item: ItemIdentifier): Boolean {
        return item.matches(player.offhandItem.itemHolder)
    }
}