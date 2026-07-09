package liltojustice.trueadaptivemusic.common.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.identifier.FluidIdentifier
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
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
        val player = Minecraft.getInstance().player ?: return false

        return (arguments.fluids.isEmpty() &&
                BuiltInRegistries.FLUID.tags.anyMatch { player.isEyeInFluid(it.key()) }) ||
                arguments.fluids.any {
                    player.isEyeInFluid(TagKey.create(Registries.FLUID, it.id))
                }
    }

    data class Arguments(val fluids: List<FluidIdentifier>): TriggerArguments()
}