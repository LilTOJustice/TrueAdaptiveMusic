package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

class EntityTypeIdentifier(id: Identifier): TypedIdentifier(id) {
    override fun toPrefixedTranslationKey(): String {
        return id.toTranslationKey("entity")
    }

    companion object: TypedIdentifierCompanion() {
        override fun getRegistryIds(): List<Identifier> {
            return Registries.ENTITY_TYPE.ids.toList()
        }
    }
}