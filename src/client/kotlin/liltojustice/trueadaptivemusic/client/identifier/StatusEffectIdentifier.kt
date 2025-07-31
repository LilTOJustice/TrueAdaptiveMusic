package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

class StatusEffectIdentifier(id: String): TypedIdentifier(id) {
    companion object: TypedIdentifierCompanion<StatusEffectIdentifier>() {
        override fun getRegistryIds(): List<Identifier> {
            return Registries.STATUS_EFFECT.ids.toList()
        }
    }
}