package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.EntityAttributeIdentifier
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft
import net.minecraft.core.registries.Registries
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.typeOf

object PlayerAttributePredicate: StaticPredicateType<PlayerAttributePredicate.Arguments>(
    "player_attribute", typeOf<Arguments>()
) {
    data class Arguments(val attribute: EntityAttributeIdentifier, val value: Double, val comparison: Comparison)
        : TriggerArguments()
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::attribute.name to "Attribute to compare for the player.",
            Arguments::value.name to "Value to compare to the attribute's value.",
            Arguments::comparison.name to "Comparison to use between the player attribute value and the given value."
        )

    override fun test(arguments: Arguments): Boolean {
        val player = Minecraft.getInstance().player ?: return false
        val registry = player.level().registryAccess().get(Registries.ATTRIBUTE).getOrNull()?.value() ?: return false
        val entityAttributeEntry = registry.get(arguments.attribute.id).getOrNull() ?: return false
        val entityAttributeValue = if (player.attributes.hasAttribute(entityAttributeEntry))
            player.attributes.getValue(entityAttributeEntry)
        else
            return false

        val value = arguments.value
        return when(arguments.comparison) {
            Comparison.Equal -> entityAttributeValue == value
            Comparison.NotEqual -> entityAttributeValue != value
            Comparison.Greater -> entityAttributeValue > value
            Comparison.GreaterOrEqual -> entityAttributeValue >= value
            Comparison.Lesser -> entityAttributeValue < value
            Comparison.LesserOrEqual -> entityAttributeValue <= value
        }
    }

    enum class Comparison {
        Equal,
        NotEqual,
        Greater,
        GreaterOrEqual,
        Lesser,
        LesserOrEqual
    }
}