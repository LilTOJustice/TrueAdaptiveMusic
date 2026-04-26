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
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Colors
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
    Text.translatableWithFallback("trueadaptivemusic.event_view", "Event View").string,
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

        if (selectedEvent?.type is ErrorEvent) {
            val result = addWidgetFromRender(
                {
                    ClickableTextWidget(
                        Text.translatableWithFallback("trueadaptivemusic.delete", "Delete").string,
                        onClick = {
                            selectedEvent = null
                            exit()
                        }
                    )
                },
                "Delete"
            )
            result.setTooltip(
                Tooltip.of(
                    Text.translatableWithFallback(
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
                    Text.translatableWithFallback("trueadaptivemusic.type", "Type").string,
                    { MusicEvent.getDisplayName(it).string },
                    startingOption = selectedEventTypeName,
                    tooltipText = Text.translatableWithFallback(
                        "trueadaptivemusic.event_type.description",
                        "Select what should trigger the music to play"
                    )
                )
            },
            "eventTypeChoice",
            row = 1)

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
                    Text.translatableWithFallback(
                        "trueadaptivemusic.music_choice", "Music Choice").string,
                    {
                        musicPack.getEditPackSoundLibrary().map { (assetName, _) -> assetName }.toMutableSet()
                            .union(
                                Registries.SOUND_EVENT.ids
                                    .map { id -> id.toString() }
                                    .filter { path -> path.contains("music.") }
                            )
                            .sorted()
                    },
                    Text.translatableWithFallback(
                        "trueadaptivemusic.select_track", "Select tracks").string,
                    selectedMusicPaths,
                    onHoverOption = { option ->
                        TAMClient.playSoundNow(option?.let { PlayableSound.of(it, soundLibrary) })
                    },
                    tooltipText = Text.translatableWithFallback(
                        "trueadaptivemusic.music_choice.description",
                        "Select any amount of music to be chosen randomly to play")
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
                    TAMClient.makeInputWidget(
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
                    TAMClient.makeInputWidget(
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
                    Text.translatableWithFallback("trueadaptivemusic.delete", "Delete").string,
                    onClick = { widget ->
                        if (!clicked) {
                            clicked = true
                            widget.setText(widget.text + '?')
                            widget.color = Colors.RED
                            val timer = Timer()
                            timer.schedule(delay = 2000) {
                                clicked = false
                                widget.setText(
                                    Text.translatableWithFallback(
                                        "trueadaptivemusic.delete", "Delete").string)
                                widget.color = Colors.WHITE
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
            Tooltip.of(
                Text.translatableWithFallback(
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