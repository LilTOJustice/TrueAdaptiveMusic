package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.resources.Identifier

class EntityTypeIdentifier(id: Identifier): TypedIdentifier(id) {
    override fun toPrefixedLanguageKey(): String {
        return id.toLanguageKey("entity")
    }

    companion object: TypedIdentifierCompanion() {
        override fun getRegistryIds(): List<Identifier> {
            return Registries.ENTITY_TYPE.ids.toList()
        }
    }
}