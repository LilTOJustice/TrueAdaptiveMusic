package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.identifier.TypedIdentifier
import liltojustice.trueadaptivemusic.client.music.MusicPack
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
    : ContainerWidget(width, height, "Event View", true, false, true, x, y) {
    private val eventTypeNameOptions = MusicEvent.getTypeNames()
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
        event?.let {
            setSelectedEventTypeName(it.getTypeName())
            eventArgs = (it.getTriggerParams().map { param -> param.value }).toMutableList()
            selectedMusicPaths = event.playableSounds.map { sound -> sound.getSoundName() }.toMutableList()
        } ?: {
            setSelectedEventTypeName(MusicEvent.getTypeNames().firstOrNull() ?: "")
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
        if (!visible) {
            return
        }

        addWidgetFromRender(
            {
                DropdownWidget(
                    eventTypeNameOptions,
                    { typeName ->  setSelectedEventTypeName(typeName) },
                    "Type",
                    startingOption = selectedEventTypeName)
            },
            "eventTypeChoice",
            row = 1)

        val musicDropdownWidget = addWidgetFromRender(
            {
                MultiSelectDropdownWidget(
                    listOf(),
                    { selected -> selectedMusicPaths = selected.toMutableList() },
                    "Music Choice",
                    {
                        musicPack.getEditPackAssets().map { (assetName, _) -> assetName }.toMutableSet()
                            .union(
                                Registries.SOUND_EVENT.ids
                                    .map { id -> id.toString() }
                                    .filter { path -> path.contains("music.") }).toList()
                    },
                    "Select a track",
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
                { InputWidgetMaker.makeWidget(screen!!, eventArgs, arg) },
                "eventArg: ${arg.name ?: arg.index}"
            )
        }

        val saveWidget = addWidgetFromRender(
            {
                ClickableTextWidget(
                    "Save",
                    onClick = {
                        assets = musicPack.getEditPackAssets()
                        val newEvent = MusicEvent.initializeFromArgs(selectedEventTypeName, *eventArgs.filterNotNull().toTypedArray())
                        newEvent.playableSounds =
                            selectedMusicPaths.mapNotNull { path -> MusicPack.toPlayableSound(assets, path) }

                        exit(newEvent)
                    })
            },
            "Save"
        ) as ClickableTextWidget

        if (selectedEvent != null) {
            addWidgetFromRender(
                {
                    ClickableTextWidget(
                        "Delete",
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
            else if (requiredEventArgs.any { arg ->
                    InputWidgetMaker.isTypedIdentifierList(arg.type)
                            && TypedIdentifier.getRegistryIdsFromType(arg.type.arguments.firstOrNull()!!.type!!)
                        .isEmpty() })
                DYNAMIC_REGISTRY_TOOLTIP
            else
                MISSING_ARGS_TOOLTIP
    }

    private fun setSelectedEventTypeName(typeName: String) {
        if (selectedEventTypeName == typeName) {
            return
        }

        selectedEventTypeName = typeName
        requiredEventArgs = MusicEvent.getRequiredArgsFromTypeName(typeName)
        eventArgs = requiredEventArgs.map { null }.toMutableList()
        clearWidgetsFromRender { childWidget -> childWidget.id in arrayOf("eventTypeChoice", "musicChoice") }
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
        private val DYNAMIC_REGISTRY_TOOLTIP =
            Tooltip.of(Text.literal("Can't access required dynamic registry. Try again while a world is loaded."))

        private val MISSING_ARGS_TOOLTIP =
            Tooltip.of(Text.literal("At least one required parameter for this type is missing."))
    }
}

