package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.widget.utility.*
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.music.MusicPack
import liltojustice.trueadaptivemusic.client.trigger.event.ErrorEvent
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Colors
import java.util.Timer
import kotlin.concurrent.schedule
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class EventViewWidget(
    width: Int,
    height: Int,
    private val musicPack: MusicPack,
    private val onSaveEvent: (newEvent: MusicEvent?, exit: Boolean) -> Unit,
    x: Int = 0,
    y: Int = 0)
    : ContainerWidget(
    width, height, "Event View", true, false, true, true, x, y) {
    private val eventTypeNameOptions = TAMClient.eventRegistry.getAllNames()
    private var selectedEventTypeName: String = eventTypeNameOptions.firstOrNull() ?: ""
    private var requiredEventArgs = listOf<KParameter>()
    private var eventArgs = mutableListOf<Any?>()
    private val requiredEventParams = MusicEvent.Parameters::class.primaryConstructor?.parameters ?: listOf()
    private var eventParams: MutableList<Any?> = requiredEventParams.map { null }.toMutableList()
    private var selectedEvent: MusicEvent? = null
    private var selectedMusicPaths = mutableListOf<String>()
    private var assets = musicPack.getEditPackAssets()

    init {
        addBackButton {
            onSaveEvent(selectedEvent, true)
        }
    }

    fun setEvent(event: MusicEvent?) {
        selectedEvent = event
        eventParams = selectedEvent?.parameters?.constructorParams()?.toMutableList()
            ?: requiredEventParams.map { null }.toMutableList()
        if (event != null) {
            setSelectedEventTypeName(event.getTypeName())
            eventArgs = (event.getTriggerArgs().map { param -> param.value }).toMutableList()
            selectedMusicPaths = event.playableSounds.map { sound -> sound.getSoundName() }.toMutableList()
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

    override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderWidget(context, mouseX, mouseY, delta)
        if (selectedEvent is ErrorEvent) {
            addWidgetFromRender(
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
                    { selected ->
                        selectedMusicPaths = selected.toMutableList()
                        save()
                    },
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
                { TAMClient.makeInputWidget(screen!!, eventArgs, arg) { save() } },
                "eventArg: ${arg.name ?: arg.index}"
            )
        }

        requiredEventParams.forEach { param ->
            addWidgetFromRender(
                { TAMClient.makeInputWidget(screen!!, eventParams, param) { save() } },
                "eventParam: ${param.name ?: param.index}"
            )
        }

        if (selectedEvent != null) {
            addWidgetFromRender(
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
        }
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

        assets = musicPack.getEditPackAssets()
        val newEvent = TAMClient.eventFactory
            .fromArgs(
                selectedEventTypeName,
                selectedMusicPaths
                    .mapNotNull { path -> MusicPack.toPlayableSound(assets, path) },
                eventParams.filterNotNull(),
                eventArgs.filterNotNull())
        selectedEvent = newEvent
        onSaveEvent(newEvent, false)
    }
}