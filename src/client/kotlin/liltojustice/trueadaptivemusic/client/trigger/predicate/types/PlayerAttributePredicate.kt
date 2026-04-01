package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.EntityAttributeIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.registry.RegistryKeys
import kotlin.jvm.optionals.getOrNull

class PlayerAttributePredicate(
    private val attribute: EntityAttributeIdentifier,
    private val value: Double,
    private val comparison: Comparison
): MusicPredicate() {
    override fun test(): Boolean {
        val player = MinecraftClient.getInstance().player ?: return false
        val registry = player.world.registryManager.getOptional(RegistryKeys.ATTRIBUTE).getOrNull()
            ?: return false
        val entityAttributeEntry =
            registry
                .mapNotNull { registry.getEntry(it) }
                .firstOrNull { entityAttributeEntry ->
                    entityAttributeEntry.matchesId(attribute.id)
                }
        val entityAttributeValue = if (player.attributes.hasAttribute(entityAttributeEntry))
            player.attributes.getValue(entityAttributeEntry)
        else
            return false

        return when(comparison) {
            Comparison.Equal -> entityAttributeValue == value
            Comparison.NotEqual -> entityAttributeValue != value
            Comparison.Greater -> entityAttributeValue > value
            Comparison.GreaterOrEqual -> entityAttributeValue >= value
            Comparison.Lesser -> entityAttributeValue < value
            Comparison.LesserOrEqual -> entityAttributeValue <= value
        }
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                PlayerAttributePredicate::attribute.name to "Attribute to compare for the player.",
                PlayerAttributePredicate::value.name to "Value to compare to the attribute's value.",
                PlayerAttributePredicate::comparison.name to "Comparison to use between the player attribute value " +
                        "and the given value."
            )
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