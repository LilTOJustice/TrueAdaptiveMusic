package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

class EntityTypeIdentifier(id: String): TypedIdentifier(id) {
    companion object: TypedIdentifierCompanion<EntityTypeIdentifier>() {
        override fun getRegistryIds(): List<Identifier> {
            return Registries.ENTITY_TYPE.ids.toList()
        }

        fun entityToTranslationKey(textKey: String): String {
            return textKey.split(".").drop(1).joinToString(".")
        }
    }
}