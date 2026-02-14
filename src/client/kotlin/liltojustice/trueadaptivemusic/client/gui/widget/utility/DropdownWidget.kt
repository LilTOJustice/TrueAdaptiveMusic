package liltojustice.trueadaptivemusic.client.gui.widget.utility

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import kotlin.math.max

class DropdownWidget<TKey>(
    options: List<Pair<TKey, String>>,
    onSelectOption: (optionKey: TKey) -> Unit,
    width: Int = 0,
    title: String = "",
    getOptions: (() -> List<Pair<TKey, String>>)? = null,
    notSelectedPlaceholder: String? = null,
    startingOption: TKey? = null,
    onHoverOption: (option: String?) -> Unit = {},
    tooltipText: Text? = null,
    x: Int = 0,
    y: Int = 0
)
    : ContainerWidget(
    width,
    0,
    "Dropdown: $title",
    false,
    false,
    false,
    true,
    x,
    y,
    true) {
    private val titleText = Text.literal(title)
    private var dropdownResultsWidget: DropdownResultsWidget<TKey>
    private val realizedWidth = width.takeUnless { width == 0 }
        ?: (
                max(
                    textRenderer.getWidth(title),
                    (options + (getOptions?.invoke() ?: listOf()))
                        .map { it.second }
                        .maxOfOrNull { option -> textRenderer.getWidth(option) } ?: 0
                ) + TEXT_WIDTH_BUFFER)
    private val textInputWidget = TextFieldWidget(
        textRenderer,
        0,
        0,
        realizedWidth,
        textRenderer.fontHeight + TEXT_HEIGHT_BUFFER,
        Text.literal("Dropdown Search")
    )
    private val selectedOptionWidget = run {
        val combinedOptions = options + (getOptions?.invoke() ?: listOf())
        ClickableTextWidget(
            notSelectedPlaceholder
                ?: combinedOptions.firstOrNull { it.first == startingOption }?.second
                ?: combinedOptions.map { it.second }.firstOrNull() ?: "",
            onClick = { screen?.focused = textInputWidget },
            isSelected = { true }
        )
    }
    private val titleTextWidget = ClickableTextWidget(titleText.string)

    init {
        tooltipText?.let {
            titleTextWidget.tooltip = Tooltip.of(it)
            selectedOptionWidget.tooltip = Tooltip.of(it)
        }
        titleTextWidget.active = false
        this.width = realizedWidth
        dropdownResultsWidget = DropdownResultsWidget(
            options,
            { option, text ->
                selectedOptionWidget.setText(text)
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

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (selectedOptionWidget.mouseClicked(mouseX, mouseY, button)) {
            screen?.focused = textInputWidget
            return true
        }

        val result = super.mouseClicked(mouseX, mouseY, button)
        textInputWidget.text = ""

        return result
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
        const val TEXT_WIDTH_BUFFER = 25
        const val TEXT_HEIGHT_BUFFER = 5
    }

    private class DropdownResultsWidget<TKey>(
        private val options: List<Pair<TKey, String>>,
        val onSelectOption: (optionKey: TKey, optionDisplay: String) -> Unit,
        private val getOptions: (() -> List<Pair<TKey, String>>)?,
        notSelectedPlaceholder: String?,
        startingOption: TKey?,
        private val onHoverOption: (option: String?) -> Unit,
        x: Int = 0,
        y: Int = 0)
        : ContainerWidget(
        0,
        0,
        "Dropdown List",
        false,
        true,
        true,
        true,
        x,
        y) {
        private var selectedOption = run {
            val combinedOptions = options + (getOptions?.invoke() ?: listOf())
            startingOption ?: combinedOptions.firstOrNull()?.first
        }
        private var searchText = ""

        init {
            if (notSelectedPlaceholder == null) {
                selectedOption?.let {
                    val option = options.firstOrNull() { optionPair -> optionPair.first == it } ?: return@let
                    onSelectOption(it, option.second)
                }
            }
        }

        override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
            if (!visible) {
                return
            }

            (getOptions?.invoke() ?: options)
                .filter { option -> option.second.lowercase().contains(searchText.lowercase()) }
                .mapIndexed { index, option ->
                    addWidgetFromRender(
                        {
                            ClickableTextWidget(
                                option.second,
                                onClick = {
                                    selectedOption = option.first
                                    onSelectOption(option.first, option.second)
                                },
                                onMouseOn = { option -> onHoverOption(option.text) },
                                onMouseOff = { option -> onHoverOption(null) })
                        },
                        option.first.hashCode().toString(),
                        index
                    )
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
            const val MAX_DISPLAYED_OPTIONS = 10
        }
    }
}