package liltojustice.trueadaptivemusic.client.music

class MusicPackValidation(preValidation: MusicPackValidation? = null)
    : ArrayList<MusicPackValidation.ValidationMessage>() {
    init {
        preValidation?.let { addAll(it) }
    }

    fun isValid(): Boolean {
        return none { message -> message.type == ValidationMessage.Type.Error }
    }

    fun addWarning(message: String) {
        add(ValidationMessage(message, ValidationMessage.Type.Warning))
    }

    fun addError(message: String) {
        add(ValidationMessage(message, ValidationMessage.Type.Error))
    }

    data class ValidationMessage(val message: String, val type: Type) {
        override fun toString(): String {
            return "$type: $message"
        }

        enum class Type {
            Warning,
            Error
        }
    }
}