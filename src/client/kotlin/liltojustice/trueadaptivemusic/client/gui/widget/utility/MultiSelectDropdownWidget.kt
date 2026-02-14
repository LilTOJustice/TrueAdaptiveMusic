package liltojustice.trueadaptivemusic.client.gui.widget.utility

import net.minecraft.client.gui.Click
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.text.Text

class MultiSelectDropdownWidget<TKey>(
    private val options: List<Pair<TKey, String>>,
    width: Int,
    private val onChange: (selected: List<TKey>) -> Unit = {},
    private val title: String = "",
    private val getOptions: (() -> List<Pair<TKey, String>>)? = null,
    private val notSelectedPlaceholder: String? = null,
    alreadySelected: List<TKey> = listOf(),
    private val onHoverOption: (option: String?) -> Unit = {},
    private val tooltipText: Text? = null,
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
    private val selected = mutableListOf<Pair<TKey, String>>()
    private var dropdownWidget: DropdownWidget<Pair<TKey, String>>? = null

    init {
        val combinedOptions = options + (getOptions?.invoke() ?: listOf())
        selected.addAll(
            alreadySelected.mapNotNull { combinedOptions.firstOrNull { option -> option.first == it } })
        onChange(selected.map { it.first })
    }

    override fun mouseClicked(click: Click, doubled: Boolean): Boolean {
        if (dropdownWidget?.mouseClicked(click, doubled) ?: false) {
            return true
        }

        return super.mouseClicked(click, doubled)
    }

    override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        dropdownWidget = addWidgetFromRender(
            {
                DropdownWidget<Pair<TKey, String>>(
                    options.map { it to it.second },
                    { option ->
                        if (selected.contains(option)) {
                            return@DropdownWidget
                        }

                        selected.add(option)
                        onChange(selected.map { it.first })
                        clearWidgetsFromRender { widget -> widget.id != "dropdown" }
                    },
                    width,
                    title,
                    getOptions?.let { opFunc -> { opFunc().map { it to it.second } } },
                    notSelectedPlaceholder,
                    null,
                    onHoverOption,
                    tooltipText,
                    x,
                    y
                )
            },
            "dropdown"
        ) as DropdownWidget<Pair<TKey, String>>

        selected.sortedBy { it.second }.map { option ->
            addWidgetFromRender(
                {
                    val widget = ClickableTextWidget(
                        option.second,
                        onClick = {
                            selected.remove(option)
                            onChange(selected.map { it.first })
                            clearWidgetsFromRender { widget -> !widget.id.startsWith("selectedOption: ") } },
                        onMouseOn = { option -> onHoverOption(option.text) },
                        onMouseOff = { option -> onHoverOption(null) })
                    widget.setTooltip(
                        Tooltip.of(
                            Text.translatableWithFallback(
                                "trueadaptivemusic.click_to_remove", "Click to remove")
                        )
                    )
                    widget
                },
                "selectedOption: $option"
            )
        }

        super.renderWidget(context, mouseX, mouseY, delta)
        fitToChildrenHeight()
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }
}