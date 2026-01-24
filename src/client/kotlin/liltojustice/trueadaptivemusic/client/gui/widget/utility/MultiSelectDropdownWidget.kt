package liltojustice.trueadaptivemusic.client.gui.widget.utility

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.text.Text

class MultiSelectDropdownWidget(
    private val options: List<String>,
    width: Int,
    private val onChange: (selected: List<String>) -> Unit = {},
    private val title: String = "",
    private val getOptions: (() -> List<String>)? = null,
    private val notSelectedPlaceholder: String? = null,
    alreadySelected: List<String> = listOf(),
    private val onHoverOption: (option: String?) -> Unit = {},
    x: Int = 0,
    y: Int = 0)
    : ContainerWidget(
    width.takeUnless { it == 0 } ?: 500,
    500,
    "Dropdown: $title",
    false,
    false,
    false,
    false,
    x,
    y,
    true) {
    private val selected = mutableListOf<String>()

    init {
        selected.addAll(alreadySelected)
        onChange(selected)
    }

    override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        addWidgetFromRender(
            {
                DropdownWidget(
                    options,
                    { option ->
                        if (selected.contains(option)) {
                            return@DropdownWidget
                        }

                        selected.add(option)
                        onChange(selected)
                        clearWidgetsFromRender { widget -> widget.id != "dropdown" }
                    },
                    width,
                    title,
                    getOptions,
                    notSelectedPlaceholder,
                    "",
                    onHoverOption,
                    x,
                    y
                )
            },
            "dropdown"
        ) as DropdownWidget

       selected.sorted().map { option ->
            addWidgetFromRender(
                {
                    val widget = ClickableTextWidget(
                        option,
                        onClick = {
                            selected.remove(option)
                            onChange(selected)
                            clearWidgetsFromRender { widget -> !widget.id.startsWith("selectedOption: ") } },
                        onMouseOn = { option -> onHoverOption(option.text) },
                        onMouseOff = { option -> onHoverOption(null) })
                    widget.setTooltip(Tooltip.of(Text.translatableWithFallback("trueadaptivemusic.click_to_remove", "Click to remove")))
                    widget
                },
                "selectedOption: $option"
            ) as ClickableTextWidget
        }

        super.renderWidget(context, mouseX, mouseY, delta)
        fitToChildrenHeight()
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }
}