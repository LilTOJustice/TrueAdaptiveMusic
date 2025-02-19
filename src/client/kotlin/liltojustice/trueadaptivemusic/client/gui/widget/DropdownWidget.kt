package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.gui.widget.DropdownWidget.DropdownResultsWidget.Companion.TEXT_HEIGHT_BUFFER
import liltojustice.trueadaptivemusic.client.gui.widget.DropdownWidget.DropdownResultsWidget.Companion.TEXT_WIDTH_BUFFER
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.gui.widget.TextWidget
import net.minecraft.text.Text

class DropdownWidget(
    options: List<String>,
    onSelectOption: (optionText: String) -> Unit,
    title: String = "",
    x: Int = 0,
    y: Int = 0)
    : ContainerWidget(0, 0, "Dropdown: $title", false, false) {
    private val titleText = Text.literal(if (title.isBlank()) "" else "$title: ")
    private val textInputWidth = options.maxOf { option -> textRenderer.getWidth(option) } + TEXT_WIDTH_BUFFER
    private lateinit var dropdownResultsWidget: DropdownResultsWidget
    private val textInputWidget = TextFieldWidget(
        textRenderer, 0, 0, textInputWidth, textRenderer.fontHeight + TEXT_HEIGHT_BUFFER, Text.literal("Dropdown Search"))
    private val selectedOptionWidget = ClickableTextWidget(
        options.firstOrNull() ?: "",
        onClick = {
            screen?.focused = textInputWidget
            dropdownResultsWidget.setSearchText("") },
        isSelected = { true })
    private val titleTextWidget = TextWidget(titleText, textRenderer)

    init {
        dropdownResultsWidget = DropdownResultsWidget(
            options,
            { option ->
                selectedOptionWidget.setText(option)
                onSelectOption(option)
            },
            { screen?.focused == textInputWidget },
            x,
            y)
        width = textInputWidth
        textInputWidget.setChangedListener { newText ->
            dropdownResultsWidget.setSearchText(newText)
        }
        addWidget(titleTextWidget, 0)
        addWidget(selectedOptionWidget, 1)
        addWidget(textInputWidget, 1)
        addWidget(dropdownResultsWidget, 2)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        val showTextInput = screen?.focused == textInputWidget
        textInputWidget.visible = showTextInput
        selectedOptionWidget.visible = !showTextInput
        super.render(context, mouseX, mouseY, delta)
        fitToChildren()
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    class DropdownResultsWidget(
        private val options: List<String>,
        val onSelectOption: (optionText: String) -> Unit,
        val isOpen: () -> Boolean,
        x: Int = 0,
        y: Int = 0)
        : ContainerWidget(0, 0, "Dropdown List", false, true, x, y) {
        private var selectedOption = options.firstOrNull() ?: ""
        private var filteredOptions = if (selectedOption.isBlank()) options else options

        init {
            width = options.maxOf { option -> textRenderer.getWidth(option) } + TEXT_WIDTH_BUFFER - 1
        }

        override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
            if (!visible || !isOpen()) {
                return
            }

            filteredOptions.forEachIndexed { index, option ->
                addWidgetFromRender(
                    {
                        ClickableTextWidget(
                            option,
                            onClick = {
                                selectedOption = option
                                onSelectOption(option)
                            })
                    },
                    option,
                    index
                )
            }
            super.render(context, mouseX, mouseY, delta)
            fitToUsedRows(MAX_DISPLAYED_OPTIONS)
        }

        fun setSearchText(searchText: String) {
            filteredOptions = options.filter { option -> option.lowercase().contains(searchText.lowercase()) }
            clearWidgetsFromRender()
        }

        override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
        }

        companion object {
            const val TEXT_WIDTH_BUFFER = 10
            const val TEXT_HEIGHT_BUFFER = 5
            const val MAX_DISPLAYED_OPTIONS = 5
        }
    }
}