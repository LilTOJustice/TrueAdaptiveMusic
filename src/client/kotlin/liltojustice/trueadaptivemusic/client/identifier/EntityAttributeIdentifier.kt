package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.client.MinecraftClient
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import kotlin.jvm.optionals.getOrNull

class EntityAttributeIdentifier(id: Identifier): TypedIdentifier(id) {
    override fun toPrefixedTranslationKey(): String {
        return id.toTranslationKey("attribute")
    }

    companion object: TypedIdentifierCompanion() {
        override fun getRegistryIds(): List<Identifier> {
            return MinecraftClient
                .getInstance().world?.registryManager
                ?.getOptional(RegistryKeys.ATTRIBUTE)
                ?.getOrNull()
                ?.ids
                ?.toList()
                ?: listOf()
        }
    }
}