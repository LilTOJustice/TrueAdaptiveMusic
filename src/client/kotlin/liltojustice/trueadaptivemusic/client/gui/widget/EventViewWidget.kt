package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.widget.utility.*
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.music.MusicPack
import liltojustice.trueadaptivemusic.client.trigger.event.ErrorEvent
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Colors
import kotlin.reflect.KParameter

class EventViewWidget(
    width: Int,
    height: Int,
    private val musicPack: MusicPack,
    private val onExitView: (newEvent: MusicEvent?) -> Unit,
    x: Int = 0,
    y: Int = 0)
    : ContainerWidget(
    width, height, "Event View", true, false, true, true, x, y) {
    private val eventTypeNameOptions = TAMClient.eventRegistry.getAllNames()
    private var selectedEventTypeName: String = eventTypeNameOptions.firstOrNull() ?: ""
    private var requiredEventArgs = listOf<KParameter>()
    private var eventArgs = mutableListOf<Any?>()
    private var selectedEvent: MusicEvent? = null
    private var selectedMusicPaths = mutableListOf<String>()
    private var assets = musicPack.getEditPackAssets()

    init {
        addBackButton { onExitView(selectedEvent) }
    }

    fun setEvent(event: MusicEvent?) {
        selectedEvent = event
        if (event != null) {
            setSelectedEventTypeName(event.getTypeName())
            eventArgs = (event.getTriggerParams().map { param -> param.value }).toMutableList()
            selectedMusicPaths = event.playableSounds.map { sound -> sound.getSoundName() }.toMutableList()
        }
        else {
            setSelectedEventTypeName(eventTypeNameOptions.firstOrNull() ?: "")
            selectedMusicPaths = mutableListOf()
        }
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (isMouseOver(mouseX, mouseY)) {
            screen?.focused = null
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        if (selectedEvent is ErrorEvent) {
            addWidgetFromRender(
                {
                    ClickableTextWidget(
                        Text.translatableWithFallback("trueadaptivemusic.delete", "Delete").string,
                        onClick = {
                            exit(null)
                        }
                    )
                },
                "Delete"
            )

            return
        }

        if (!visible) {
            return
        }

        addWidgetFromRender(
            {
                DropdownWidget(
                    eventTypeNameOptions,
                    { typeName ->  setSelectedEventTypeName(typeName) },
                    width / 2,
                    Text.translatableWithFallback("trueadaptivemusic.type", "Type").string,
                    startingOption = selectedEventTypeName)
            },
            "eventTypeChoice",
            row = 1)

        val musicDropdownWidget = addWidgetFromRender(
            {
                MultiSelectDropdownWidget(
                    listOf(),
                    width,
                    { selected -> selectedMusicPaths = selected.toMutableList() },
                    Text.translatableWithFallback(
                        "trueadaptivemusic.music_choice", "Music Choice").string,
                    {
                        musicPack.getEditPackAssets().map { (assetName, _) -> assetName }.toMutableSet()
                            .union(
                                Registries.SOUND_EVENT.ids
                                    .map { id -> id.toString() }
                                    .filter { path -> path.contains("music.") }).toList()
                    },
                    Text.translatableWithFallback(
                        "trueadaptivemusic.select_track", "Select a track").string,
                    selectedMusicPaths,
                    onHoverOption = { option ->
                        TAMClient.playSoundNow(option.let { MusicPack.toPlayableSound(assets, it) }) })
            },
            "musicChoice"
        )

        if (isMouseOver(mouseX.toDouble(), mouseY.toDouble())
            && !musicDropdownWidget.isMouseOver(mouseX.toDouble(), mouseY.toDouble())) {
            TAMClient.playSoundNow(null)
        }

        requiredEventArgs.forEach { arg ->
            addWidgetFromRender(
                { TAMClient.makeInputWidget(screen!!, eventArgs, arg) },
                "eventArg: ${arg.name ?: arg.index}"
            )
        }

        val saveWidget = addWidgetFromRender(
            {
                ClickableTextWidget(
                    "Save",
                    onClick = {
                        assets = musicPack.getEditPackAssets()
                        val newEvent = TAMClient.eventFactory
                            .fromArgs(
                                selectedEventTypeName,
                                selectedMusicPaths
                                    .mapNotNull { path -> MusicPack.toPlayableSound(assets, path) },
                                *eventArgs.filterNotNull().toTypedArray())

                        exit(newEvent)
                    })
            },
            "Save"
        ) as ClickableTextWidget

        if (selectedEvent != null) {
            addWidgetFromRender(
                {
                    ClickableTextWidget(
                        Text.translatableWithFallback("trueadaptivemusic.delete", "Delete").string,
                        onClick = {
                            exit(null)
                        }
                    )
                },
                "Delete"
            )
        }

        saveWidget.active = eventArgs.filterNotNull().size == requiredEventArgs.size
        saveWidget.color = if (saveWidget.active) Colors.WHITE else Colors.RED
        saveWidget.tooltip =
            if (saveWidget.active)
                null
            else
                MISSING_ARGS_TOOLTIP
    }

    private fun setSelectedEventTypeName(typeName: String) {
        if (selectedEventTypeName == typeName) {
            return
        }

        selectedEventTypeName = typeName
        requiredEventArgs = TAMClient.eventFactory.getRequiredArgs(typeName)
        eventArgs = requiredEventArgs.map { null }.toMutableList()
        clearWidgetsFromRender()
    }

    private fun exit(event: MusicEvent?) {
        selectedEvent = null
        selectedMusicPaths = mutableListOf()
        selectedEventTypeName = eventTypeNameOptions.firstOrNull() ?: ""
        requiredEventArgs = emptyList()
        eventArgs = mutableListOf()
        clearWidgetsFromRender()
        onExitView(event)
    }

    companion object {
        private val MISSING_ARGS_TOOLTIP =
            Tooltip.of(
                Text.translatableWithFallback(
                    "trueadaptivemusic.missing_parameter",
                    "At least one required parameter for this type is missing."))
    }
}