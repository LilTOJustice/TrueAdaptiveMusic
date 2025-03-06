package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

class EntityTypeIdentifier(id: String): TypedIdentifier(id) {
    companion object: TypedIdentifierCompanion<EntityTypeIdentifier>() {
        override fun getRegistryIds(): List<Identifier> {
            return Registry.ENTITY_TYPE.ids.toList()
        }
    }
}