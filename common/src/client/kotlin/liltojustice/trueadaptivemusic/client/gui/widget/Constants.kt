package liltojustice.trueadaptivemusic.client.gui.widget

import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Component

object Constants {
    val MUSIC_CHOICE_TEXT: MutableComponent = Component.translatableWithFallback(
        "trueadaptivemusic.music_choice", "Music Choice")
    val SELECT_TRACKS_TEXT: MutableComponent = Component
        .translatableWithFallback("trueadaptivemusic.select_track", "Select tracks")
}