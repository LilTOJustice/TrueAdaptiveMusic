package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.Constants.Companion.ALLOWED_FILE_TYPES
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

internal object Constants {
    val ALLOWED_FILE_TYPES_TEXT = "${
        Component
            .translatableWithFallback("trueadaptivemusic.allowed_file_types", "Allowed file types")
            .string
    }: ${ALLOWED_FILE_TYPES.joinToString(", ")}"
    val MUSIC_CHOICE_TEXT: MutableComponent = Component.translatableWithFallback(
        "trueadaptivemusic.music_choice", "Music Choice")
    val SELECT_TRACKS_TEXT = Component
        .translatableWithFallback("trueadaptivemusic.select_track", "Select tracks")
    val MUSIC_CHOICE_TOOLTIP_TEXT: MutableComponent = Component.translatableWithFallback(
        "trueadaptivemusic.music_choice.description",
        "Select any amount of music to be chosen randomly to play"
    ).append("\n\n").append(ALLOWED_FILE_TYPES_TEXT)
}