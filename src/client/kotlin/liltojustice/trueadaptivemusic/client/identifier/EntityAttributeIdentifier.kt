package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.client.Minecraft
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import kotlin.jvm.optionals.getOrNull

class EntityAttributeIdentifier(id: Identifier): TypedIdentifier(id) {
    override fun toPrefixedLanguageKey(): String {
        return id.toLanguageKey("attribute")
    }

    companion object: TypedIdentifierCompanion() {
        override fun getRegistryIds(): List<Identifier> {
            return Minecraft
                .getInstance().world?.registryManager
                ?.getOptional(Registries.ATTRIBUTE)
                ?.getOrNull()
                ?.ids
                ?.toList()
                ?: listOf()
        }
    }
}