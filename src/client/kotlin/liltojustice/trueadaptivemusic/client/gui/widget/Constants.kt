package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.Constants.Companion.ALLOWED_FILE_TYPES
import net.minecraft.text.MutableText
import net.minecraft.text.Text

internal object Constants {
    val ALLOWED_FILE_TYPES_TEXT = "${
        Text
            .translatableWithFallback("trueadaptivemusic.allowed_file_types", "Allowed file types")
            .string
    }: ${ALLOWED_FILE_TYPES.joinToString(", ")}"
    val MUSIC_CHOICE_TEXT: MutableText = Text.translatableWithFallback(
        "trueadaptivemusic.music_choice", "Music Choice")
    val SELECT_TRACKS_TEXT: MutableText = Text
        .translatableWithFallback("trueadaptivemusic.select_track", "Select tracks")
    val MUSIC_CHOICE_TOOLTIP_TEXT: MutableText = Text.translatableWithFallback(
        "trueadaptivemusic.music_choice.description",
        "Select any amount of music to be chosen randomly to play"
    ).append("\n\n").append(ALLOWED_FILE_TYPES_TEXT)
}