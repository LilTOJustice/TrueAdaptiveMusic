package liltojustice.trueadaptivemusic.client.gui.widget

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

object Constants {
    val MUSIC_CHOICE_TEXT: MutableComponent = Component.translatableWithFallback(
        "trueadaptivemusic.music_choice", "Music Choice")
    val SELECT_TRACKS_TEXT = Component
        .translatableWithFallback("trueadaptivemusic.select_track", "Select tracks")
}