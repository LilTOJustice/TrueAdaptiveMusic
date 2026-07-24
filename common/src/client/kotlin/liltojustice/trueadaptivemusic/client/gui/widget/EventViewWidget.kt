package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.widget.utility.*
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import liltojustice.trueadaptivemusic.client.trigger.MusicTriggerException
import liltojustice.trueadaptivemusic.client.trigger.event.ErrorEvent
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusicapi.TAMAPI
import liltojustice.trueadaptivemusicapi.widget.EmptyClickableWidget
import liltojustice.trueadaptivemusicapi.widget.WidgetArg
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import net.minecraft.resources.ResourceLocation
import java.util.Timer
import kotlin.concurrent.schedule
import kotlin.reflect.full.primaryConstructor

class EventViewWidget(
    width: Int,
    height: Int,
    private val musicPack: MusicPack,
    private val onSaveEvent: (newEvent: MusicEvent<*>?, exit: Boolean) -> Unit,
    x: Int = 0,
    y: Int = 0)
    : ContainerWidget(
    width,
    height,
    Component.translatableWithFallback("trueadaptivemusic.event_view", "Event View").string,
    true,
    false,
    true,
    false,
    true,
    x,
    y) {
    private val eventTypeNameOptions = TAMAPI.getEventTypeNames()
    private var selectedEventTypeName: String = eventTypeNameOptions.firstOrNull() ?: ""
    private var requiredEventArgs = listOf<WidgetArg>()
    private var eventArgs = mutableListOf<Any?>()
    private val requiredEventParams = MusicEvent.Parameters::class.primaryConstructor?.parameters
        ?.map { WidgetArg.of(it) } ?: listOf()
    private var eventParams: MutableList<Any?> = requiredEventParams.map { null }.toMutableList()
    private var selectedEvent: MusicEvent<*>? = null
    private var selectedMusicPaths = mutableListOf<String>()
    private var soundLibrary = musicPack.getEditPackSoundLibrary()

    init {
        addBackButton {
            onSaveEvent(selectedEvent, true)
        }
    }

    fun setEvent(event: MusicEvent<*>?) {
        selectedEvent = event
        eventParams = selectedEvent?.parameters?.getMusicParams()?.map { param -> param.value }?.toMutableList()
            ?: requiredEventParams.map { null }.toMutableList()
        if (event != null) {
            if (event.type !is ErrorEvent) {
                setSelectedEventTypeName(event.type.typeName)
                eventArgs = (MusicTrigger.getTriggerArgs(event.arguments).map { arg -> arg.value }).toMutableList()
            }

            selectedMusicPaths = event.music.map { sound -> sound.getSoundName() }.toMutableList()
        }
        else {
            selectedMusicPaths = mutableListOf()
        }
        clearWidgetsFromRender { false }
    }

    override fun updateWidgetNarration(builder: NarrationElementOutput) {
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (isMouseOver(mouseX, mouseY)) {
            screen?.focused = null
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderWidget(context, mouseX, mouseY, delta)
        if (!visible) {
            return
        }

        if (selectedEvent?.type is ErrorEvent) {
            val result = addWidgetFromRender(
                {
                    ClickableTextWidget(
                        Component.translatableWithFallback("trueadaptivemusic.delete", "Delete").string,
                        onClick = {
                            selectedEvent = null
                            exit()
                        }
                    )
                },
                "Delete"
            )
            result.setTooltip(
                Tooltip.create(
                    Component.translatableWithFallback(
                        "trueadaptivemusic.delete_event_description", "Delete this event")
                )
            )

            return
        }

        addWidgetFromRender(
            {
                DropdownWidget(
                    eventTypeNameOptions,
                    { typeName ->  setSelectedEventTypeName(typeName) },
                    width / 2,
                    Component.translatableWithFallback("trueadaptivemusic.type", "Type").string,
                    { MusicEvent.getDisplayName(it).string },
                    startingOption = selectedEventTypeName,
                    tooltipText = Component.translatableWithFallback(
                        "trueadaptivemusic.event_type.description",
                        "Select what should trigger the music to play"
                    )
                )
            },
            "eventTypeChoice",
            row = 1
        )

        val musicDropdownWidget = addWidgetFromRender(
            {
                MultiSelectDropdownWidget(
                    listOf(),
                    width,
                    null,
                    { selected ->
                        selectedMusicPaths = selected.toMutableList()
                        save()
                    },
                    Constants.MUSIC_CHOICE_TEXT.string,
                    {
                        musicPack.getEditPackSoundLibrary().map { (assetName, _) -> assetName }.toMutableSet()
                            .union(
                                BuiltInRegistries.SOUND_EVENT.keySet()
                                    .map { id -> id.toString() }
                                    .filter { path -> path.contains("music.") }
                            )
                            .sorted()
                    },
                    Constants.SELECT_TRACKS_TEXT.string,
                    selectedMusicPaths,
                    { option ->
                        TAMClient.playSoundNow(option?.let { PlayableSound.of(it, soundLibrary) })
                    },
                    TAMClient.allowedFileTypesText(),
                    customCreator = { text -> ResourceLocation.tryParse(text)?.toString() }
                )
            },
            "musicChoice"
        )

        if (isMouseOver(mouseX.toDouble(), mouseY.toDouble())
            && !musicDropdownWidget.isMouseOver(mouseX.toDouble(), mouseY.toDouble())) {
            TAMClient.playSoundNow(null)
        }

        requiredEventArgs.forEach { arg ->
            addWidgetFromRender(
                {
                    TAMAPI.makeInputWidget(
                        screen!!,
                        eventArgs,
                        arg,
                        arg.name
                            ?.let { MusicEvent.getArgDisplayName(selectedEventTypeName, it) },
                        arg.name
                            ?.let { MusicEvent.getArgDescription(selectedEventTypeName, it) }
                    ) { save() }
                },
                "eventArg: ${arg.name ?: arg.index}"
            )
        }

        requiredEventParams.forEach { param ->
            addWidgetFromRender(
                {
                    TAMAPI.makeInputWidget(
                        screen!!,
                        eventParams,
                        param,
                        param.name?.let { MusicEvent.Parameters.getParamDisplayName(it) },
                        param.name?.let { MusicEvent.Parameters.getParamDescription(it) }
                    ) { save() }
                },
                "eventParam: ${param.name ?: param.index}"
            )
        }

        addWidgetFromRender(
            {
                EmptyClickableWidget()
            },
            "deleteSpacer"
        )

        val result = addWidgetFromRender(
            {
                var clicked = false
                ClickableTextWidget(
                    Component.translatableWithFallback("trueadaptivemusic.delete", "Delete").string,
                    onClick = { widget ->
                        if (!clicked) {
                            clicked = true
                            widget.setText(widget.text + '?')
                            widget.color = CommonColors.RED
                            val timer = Timer()
                            timer.schedule(delay = 2000) {
                                clicked = false
                                widget.setText(
                                    Component.translatableWithFallback(
                                        "trueadaptivemusic.delete", "Delete").string)
                                widget.color = CommonColors.WHITE
                            }

                            return@ClickableTextWidget
                        }

                        selectedEvent = null
                        exit()
                    }
                )
            },
            "Delete"
        )
        result.setTooltip(
            Tooltip.create(
                Component.translatableWithFallback(
                    "trueadaptivemusic.delete_event_description", "Delete this event")
            )
        )

        addWidgetFromRender(
            {
                EmptyClickableWidget()
            },
            "finalSpacer"
        )
    }

    private fun setSelectedEventTypeName(typeName: String) {
        if (selectedEventTypeName == typeName) {
            return
        }

        selectedEventTypeName = typeName
        requiredEventArgs = TAMAPI.getEventTypeArguments(typeName).map { WidgetArg.of(it) }
        eventArgs = requiredEventArgs.map { null }.toMutableList()
        clearWidgetsFromRender()
    }

    private fun exit() {
        val outEvent = selectedEvent
        selectedEvent = null
        selectedMusicPaths = mutableListOf()
        selectedEventTypeName = eventTypeNameOptions.firstOrNull() ?: ""
        requiredEventArgs = emptyList()
        eventArgs = mutableListOf()
        clearWidgetsFromRender()
        onSaveEvent(outEvent, true)
    }

    private fun save() {
        if (eventArgs.filterNotNull().size != requiredEventArgs.size
            || eventParams.filterNotNull().size != requiredEventParams.size) {
            return
        }

        soundLibrary = musicPack.getEditPackSoundLibrary()
        val eventType = TAMAPI.getEventType(selectedEventTypeName)
            ?: throw MusicTriggerException("Unknown event type '$selectedEventTypeName'")
        val newEvent = TAMClient.musicEventFactory
            .fromArgs(
                eventType,
                TAMAPI.makeEventArguments(eventType, eventArgs.filterNotNull()),
                selectedMusicPaths.mapNotNull { path -> PlayableSound.of(path, soundLibrary) },
                MusicEvent.Parameters.fromArgs(eventParams.filterNotNull())
            )
        selectedEvent = newEvent
        onSaveEvent(newEvent, false)
    }
}