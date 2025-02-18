package liltojustice.trueadaptivemusic.client.gui.widget

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder

class DropdownWidget(
    val options: List<String>,
    val onSelectOption: (optionText: String) -> Unit,
    val title: String = "",
    x: Int = 0,
    y: Int = 0)
    : ContainerWidget(0, 0, "Dropdown: $title", false, true, x, y) {
    var isOpen = false
    var selectedOption = options.firstOrNull() ?: ""

    init {
        width = options.maxOf { option -> textRenderer.getWidth(option) } + TEXT_WIDTH_BUFFER
        height = textRenderer.fontHeight + TEXT_HEIGHT_BUFFER
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        if (!enabled) {
            return
        }

        if (isOpen) {
            options.forEachIndexed { index, option ->
                addWidgetFromRender(
                    {
                        ClickableTextWidget(
                            option,
                            onClick = {
                                selectedOption = option
                                close()
                                onSelectOption(option)
                            })
                    },
                    option,
                    index
                )
            }
        }
        else {
            addWidgetFromRender(
                { ClickableTextWidget(selectedOption, onClick = { open() }) }, selectedOption, 0)
        }
        fitToUsedRows(MAX_DISPLAYED_OPTIONS)
    }

    private fun open() {
        clearWidgetsFromRender()
        isOpen = true
    }

    private fun close() {
        clearWidgetsFromRender()
        isOpen = false
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    companion object {
        private const val TEXT_WIDTH_BUFFER = 10
        private const val TEXT_HEIGHT_BUFFER = 5
        private const val MAX_DISPLAYED_OPTIONS = 5
    }
}