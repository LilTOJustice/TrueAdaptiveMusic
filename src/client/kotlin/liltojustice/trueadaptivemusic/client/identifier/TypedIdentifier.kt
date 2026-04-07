package liltojustice.trueadaptivemusic.client.identifier

import liltojustice.trueadaptivemusic.text.StringExtensions.prettify
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import kotlin.reflect.KType
import kotlin.reflect.full.*
import kotlin.text.split

sealed class TypedIdentifier(val id: Identifier) {
    val path: String
        get() = id.path
    val namespace: String
        get() = id.namespace

    abstract fun toPrefixedLanguageKey(): String

    override fun equals(other: Any?): Boolean {
        return super.equals(other) || (other as? TypedIdentifier)?.id == id
    }

    fun toLanguageKey(prefix: String): String {
        return id.toLanguageKey(prefix)
    }

    fun prettify(): String {
        val languageKey = toPrefixedLanguageKey()
        val translatedString = Component.translatable(languageKey).string
        return if (translatedString != languageKey) {
            "${toString().split(":")[0].replaceFirstChar { it.uppercase() }} - $translatedString"
        }
        else {
            toString().prettify()
        }
    }

    companion object: TypedIdentifierCompanion() {
        override fun getRegistryIds(): List<Identifier> {
            throw TypedIdentifierException(
                "Attempt to get type name from abstract ${TypedIdentifier::class.simpleName}.")
        }

        fun getRegistryIdsFromType(type: KType): List<Identifier> {
            val typeCompanion = TypedIdentifierCompanion::class.sealedSubclasses
                .firstOrNull { subclass -> subclass.qualifiedName?.contains(type.toString()) ?: false }
                ?: throw TypedIdentifierException("Failed to find valid companion for $type. " +
                        "Ensure it has a companion object implementing the " +
                        "${TypedIdentifierCompanion::class.simpleName} interface.")
            return (typeCompanion.functions.firstOrNull { f -> f.name == Companion::getRegistryIds.name }
                ?.call(typeCompanion.objectInstance) as? List<*>)?.filterIsInstance<Identifier>()
                ?: throw TypedIdentifierException(
                    "Failed to get registry ids from identifier type ${type}. " +
                            "Ensure it has a companion object implementing the " +
                            "${TypedIdentifierCompanion::class.simpleName} interface.")
        }
    }

    sealed class TypedIdentifierCompanion {
        abstract fun getRegistryIds(): List<Identifier>
        fun initializeFromIdString(type: KType, id: String): TypedIdentifier {
            return TypedIdentifier::class.sealedSubclasses
                .firstOrNull { subclass ->
                    subclass.createType(
                        type.arguments, type.isMarkedNullable, type.annotations) == type }
                ?.primaryConstructor?.call(Identifier.parse(id))
                ?: throw TypedIdentifierException("Failed to initialize ${this::class.simpleName} from id $id")
        }
    }

    override fun toString(): String {
        return id.toString()
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + namespace.hashCode()
        return result
    }
}