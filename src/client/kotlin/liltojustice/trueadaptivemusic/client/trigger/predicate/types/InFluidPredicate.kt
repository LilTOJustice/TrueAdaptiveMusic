package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.identifier.FluidIdentifier
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.MinecraftClient
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.TagKey
import kotlin.reflect.typeOf

object InFluidPredicate: StaticPredicateType<InFluidPredicate.Arguments>(
    "in_fluid", typeOf<Arguments>()
) {
    override val tickRate: Int
        get() = 4
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::fluids.name to "Select all fluids the music should play for. If none, any fluid will trigger."
        )

    override fun test(arguments: Arguments): Boolean {
        val player = MinecraftClient.getInstance().player ?: return false

        return (arguments.fluids.isEmpty() &&
                Registries.FLUID.streamTags().anyMatch { player.isSubmergedIn(it) }) ||
                arguments.fluids.any {
                    player.isSubmergedIn(TagKey.of(Registries.FLUID.key, it.id))
                }
    }

    data class Arguments(val fluids: List<FluidIdentifier>): TriggerArguments()
}