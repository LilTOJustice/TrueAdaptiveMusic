package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.identifier.FluidIdentifier
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.PredicateType
import liltojustice.trueadaptivemusicapi.trigger.state.TriggerState
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.tags.TagKey
import kotlin.reflect.typeOf

object OnFluidPredicate: PredicateType<OnFluidPredicate.Arguments, OnFluidPredicate.State>(
    "on_fluid", typeOf<Arguments>()
) {
    override val tickRate: Int
        get() = 2
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::fluids.name to "Select all fluids the music should play for. If none, any fluid will trigger.",
            Arguments::includeVehicle.name to "If checked, the entity the player is riding will also allow this to " +
                    "trigger."
        )
    private const val GRACE_PERIOD_TICKS = 6

    override fun test(arguments: Arguments, state: State): Boolean {
        val player = Minecraft.getInstance().player ?: return false
        val result = entityOnFluids(player, arguments.fluids) ||
                (arguments.includeVehicle && player.vehicle?.let { entityOnFluids(it, arguments.fluids) } ?: false)

        if (result) {
            state.tickBuffer = GRACE_PERIOD_TICKS
        }
        else if (state.tickBuffer-- > 0) {
            return true
        }

        return result
    }

    override fun createState(arguments: Arguments): State {
        return State()
    }

    private fun entityOnFluids(entity: Entity, fluids: List<FluidIdentifier>): Boolean {
        return (fluids.isEmpty() &&
                BuiltInRegistries.FLUID.listTags().anyMatch { entity.getFluidHeight(it.key()) != 0.0 }) ||
                fluids.any {
                    entity.getFluidHeight(TagKey.create(BuiltInRegistries.FLUID.key(), it.id)) != 0.0
                }
    }

    data class Arguments(val fluids: List<FluidIdentifier>, val includeVehicle: Boolean): TriggerArguments()
    data class State(var tickBuffer: Int = 0): TriggerState()
}