package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.predicate.MusicPredicate
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder

class DropdownWidget(
    width: Int,
    height: Int,
    options: List<String>,
    onSelectOption: (optionText: String) -> Unit,
    x: Int = 0,
    y: Int = 0)
    : ContainerWidget(width, height, "Pack Structure", false, true, x, y) {
    init {
        MusicPredicate.getTypeNames().forEachIndexed { index, typeName ->
            addWidget(ClickableTextWidget(typeName, onClick = { onSelectOption(typeName) }), index)
        }
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }
}