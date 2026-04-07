package liltojustice.trueadaptivemusic.client.gui.widget.utility

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component

class MultiSelectDropdownWidget<TKey>(
    private val options: List<TKey>,
    width: Int,
    private val getDisplay: ((TKey) -> String)? = null,
    private val onChange: (selected: List<TKey>) -> Unit = {},
    private val title: String = "",
    private val getOptions: (() -> List<TKey>)? = null,
    private val notSelectedPlaceholder: String? = null,
    alreadySelected: List<TKey> = listOf(),
    private val onHoverOption: (option: String?) -> Unit = {},
    private val tooltipText: Component? = null,
    x: Int = 0,
    y: Int = 0
)
    : ContainerWidget(
    width.takeUnless { it == 0 } ?: 500,
    500,
    "Dropdown: $title",
    false,
    false,
    false,
    false,
    false,
    x,
    y,
    true) {
    private val selected = mutableListOf<TKey>()

    init {
        selected.addAll(alreadySelected)
        onChange(selected)
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        val result = super.mouseClicked(event, doubleClick)

        if (!result) {
            visitWidgets { it.mouseClicked(event, doubleClick) }
        }

        return result
    }

    override fun extractWidgetRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
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
                    getDisplay,
                    getOptions,
                    notSelectedPlaceholder,
                    null,
                    onHoverOption,
                    tooltipText,
                    x,
                    y
                )
            },
            "dropdown"
        )

        selected.map { it to (getDisplay?.invoke(it) ?: it.toString()) }.sortedBy { it.second }.map { option ->
            addWidgetFromRender(
                {
                    val widget = ClickableTextWidget(
                        option.second,
                        onClick = {
                            onHoverOption(null)
                            selected.remove(option.first)
                            onChange(selected)
                            clearWidgetsFromRender { widget -> !widget.id.startsWith("selectedOption: ") } },
                        onMouseOn = { option -> onHoverOption(option.text) },
                        onMouseOff = { option -> onHoverOption(null) })
                    widget.setTooltip(
                        Tooltip.create(
                            Component.translatableWithFallback(
                                "trueadaptivemusic.click_to_remove", "Click to remove")
                        )
                    )
                    widget
                },
                "selectedOption: $option"
            )
        }

        super.extractWidgetRenderState(graphics, mouseX, mouseY, a)
        fitToChildrenHeight()
    }

    override fun updateWidgetNarration(output: NarrationElementOutput) {
    }
}