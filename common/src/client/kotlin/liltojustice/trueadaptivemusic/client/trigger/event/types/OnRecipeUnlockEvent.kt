package liltojustice.trueadaptivemusic.client.trigger.event.types

import liltojustice.trueadaptivemusicapi.identifier.ItemIdentifier
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.event.input.EventInput
import liltojustice.trueadaptivemusicapi.trigger.event.type.StaticEventType
import net.minecraft.core.Holder
import net.minecraft.world.item.Item
import kotlin.reflect.typeOf

object OnRecipeUnlockEvent
    : StaticEventType<OnRecipeUnlockEvent.Arguments, OnRecipeUnlockEvent.Input>(
    "on_recipe_unlock",
    typeOf<Arguments>()
) {
    override fun validate(
        arguments: Arguments,
        input: Input
    ): Boolean {
        val items = arguments.items ?: return true

        return items.isEmpty() || items.any { item -> item.matches(input.unlockedItem) }
    }

    data class Arguments(val items: List<ItemIdentifier>?): TriggerArguments()

    data class Input(val unlockedItem: Holder<Item>): EventInput()
}