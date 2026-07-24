package liltojustice.trueadaptivemusic.client.gui.widget.utility

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.components.Tooltip
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
    y: Int = 0,
    private val customCreator: ((text: String) -> TKey?)? = null
): ContainerWidget(
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
    true
) {
    private val selected = mutableListOf<TKey>()

    init {
        selected.addAll(alreadySelected)
        onChange(selected)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val result = super.mouseClicked(mouseX, mouseY, button)

        if (!result) {
            visitWidgets { it.mouseClicked(mouseX, mouseY, button) }
        }

        return result
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
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
                    y,
                    customCreator
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
                        onMouseOff = { _ -> onHoverOption(null) }
                    )
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

        super.renderWidget(context, mouseX, mouseY, delta)
        fitToChildrenHeight()
    }

    override fun updateWidgetNarration(builder: NarrationElementOutput) {
    }
}