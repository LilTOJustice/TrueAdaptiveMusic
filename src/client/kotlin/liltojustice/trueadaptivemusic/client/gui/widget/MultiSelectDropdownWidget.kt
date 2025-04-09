package liltojustice.trueadaptivemusic.client.gui.widget

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder

class MultiSelectDropdownWidget(
    private val options: List<String>,
    private val onChange: (selected: List<String>) -> Unit = {},
    private val title: String = "",
    private val getOptions: (() -> List<String>)? = null,
    private val notSelectedPlaceholder: String? = null,
    alreadySelected: List<String> = listOf(),
    private val onHoverOption: (option: String) -> Unit = {},
    x: Int = 0,
    y: Int = 0)
    : ContainerWidget(
    500,
    500,
    "Dropdown: $title",
    false,
    false,
    false,
    x,
    y,
    true) {
    private val selected = mutableListOf<String>()
    private var hoveredWidget: ClickableTextWidget? = null

    init {
        selected.addAll(alreadySelected)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        addWidgetFromRender(
            {
                DropdownWidget(
                    options,
                    { option ->
                        selected.add(option)
                        onChange(selected)
                        clearWidgetsFromRender { widget -> widget.id != "dropdown" }
                    },
                    title,
                    getOptions,
                    notSelectedPlaceholder,
                    "",
                    onHoverOption,
                    x,
                    y)
            },
            "dropdown"
        ) as DropdownWidget

        val selectedWidgets = selected.sorted().map { option ->
            addWidgetFromRender(
                {
                    ClickableTextWidget(option, onClick = {
                        selected.remove(option)
                        onChange(selected)
                        clearWidgetsFromRender { widget -> !widget.id.startsWith("selectedOption: ") }
                    })
                },
                "selectedOption: $option"
            ) as ClickableTextWidget
        }

        val newHoveredWidget = selectedWidgets.firstOrNull { widget ->
            childVisible(widget) && widget.isMouseOver(mouseX.toDouble(), mouseY.toDouble())
        }

        if (newHoveredWidget != null && newHoveredWidget != hoveredWidget) {
            hoveredWidget = newHoveredWidget
            onHoverOption(hoveredWidget!!.text)
        }
        else if (newHoveredWidget == null) {
            hoveredWidget = null
        }

        super.render(context, mouseX, mouseY, delta)
        fitToChildrenHeight()
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }
}