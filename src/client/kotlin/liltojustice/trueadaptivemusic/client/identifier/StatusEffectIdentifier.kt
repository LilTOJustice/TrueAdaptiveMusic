package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.resources.Identifier

class StatusEffectIdentifier(id: Identifier): TypedIdentifier(id) {
    override fun toPrefixedLanguageKey(): String {
        return id.toLanguageKey("effect")
    }

    companion object: TypedIdentifierCompanion() {
        override fun getRegistryIds(): List<Identifier> {
            return Registries.STATUS_EFFECT.ids.toList()
        }
    }
}