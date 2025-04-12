package liltojustice.trueadaptivemusic.client.gui.widget

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text

class DropdownWidget(
    options: List<String>,
    onSelectOption: (optionText: String) -> Unit,
    title: String = "",
    getOptions: (() -> List<String>)? = null,
    notSelectedPlaceholder: String? = null,
    startingOption: String = "",
    onHoverOption: (option: String) -> Unit = {},
    x: Int = 0,
    y: Int = 0)
    : ContainerWidget(
    0,
    0,
    "Dropdown: $title",
    false,
    false,
    true,
    x,
    y,
    true) {
    private val titleText = Text.literal(if (title.isBlank()) "" else "$title: ")
    private var textInputWidth = (
            if (notSelectedPlaceholder != null)
                textRenderer.getWidth(notSelectedPlaceholder)
            else
                (options.maxOfOrNull { option -> textRenderer.getWidth(option) } ?: 0)) +
            TEXT_WIDTH_BUFFER
    private var dropdownResultsWidget: DropdownResultsWidget
    private val textInputWidget = TextFieldWidget(
        textRenderer,
        0,
        0,
        textInputWidth,
        textRenderer.fontHeight + TEXT_HEIGHT_BUFFER,
        Text.literal("Dropdown Search"))
    private val selectedOptionWidget = ClickableTextWidget(
        notSelectedPlaceholder ?: startingOption.ifEmpty { null } ?: options.firstOrNull() ?: "",
        onClick = { screen?.focused = textInputWidget },
        isSelected = { true })
    private val titleTextWidget = ClickableTextWidget(titleText.string)

    init {
        titleTextWidget.active = false
        width = textInputWidth
        dropdownResultsWidget = DropdownResultsWidget(
            options,
            { option ->
                selectedOptionWidget.setText(option)
                onSelectOption(option)
            },
            getOptions,
            notSelectedPlaceholder,
            startingOption,
            onHoverOption,
            x,
            y)
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
        dropdownResultsWidget.visible = screen?.focused == textInputWidget
        dropdownResultsWidget.width = width
        super.render(context, mouseX, mouseY, delta)
        fitToChildrenHeight()
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    companion object {
        const val TEXT_WIDTH_BUFFER = 15
        const val TEXT_HEIGHT_BUFFER = 5
    }

    private class DropdownResultsWidget(
        private val options: List<String>,
        val onSelectOption: (optionText: String) -> Unit,
        private val getOptions: (() -> List<String>)?,
        notSelectedPlaceholder: String?,
        startingOption: String,
        private val onHoverOption: (option: String) -> Unit,
        x: Int = 0,
        y: Int = 0)
        : ContainerWidget(
        0,
        0,
        "Dropdown List",
        false,
        true,
        true,
        x,
        y) {
        private var selectedOption = startingOption.ifEmpty { null } ?: notSelectedPlaceholder ?: options.firstOrNull() ?: ""
        private var searchText = ""
        private var hoveredWidget: ClickableTextWidget? = null

        init {
            if (selectedOption.isNotBlank() && notSelectedPlaceholder == null) {
                onSelectOption(selectedOption)
            }
        }

        override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
            if (!visible) {
                return
            }

            val optionsWidgets = (getOptions?.invoke() ?: options)
                .filter { option -> option.lowercase().contains(searchText.lowercase()) }
                .mapIndexed { index, option ->
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
                    ) as ClickableTextWidget

                }

            val newHoveredWidget = optionsWidgets
                .firstOrNull { widget -> childVisible(widget) && widget.isMouseOver(mouseX.toDouble(), mouseY.toDouble()) }

            if (newHoveredWidget != null && newHoveredWidget != hoveredWidget) {
                hoveredWidget = newHoveredWidget
                onHoverOption(hoveredWidget!!.text)
            }
            else if (newHoveredWidget == null) {
                hoveredWidget = null
            }

            fitToUsedRows(MAX_DISPLAYED_OPTIONS)
            super.render(context, mouseX, mouseY, delta)
        }

        fun setSearchText(searchText: String) {
            this.searchText = searchText
            clearWidgetsFromRender()
        }

        override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
        }

        companion object {
            const val MAX_DISPLAYED_OPTIONS = 5
        }
    }
}