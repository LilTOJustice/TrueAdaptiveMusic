package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.extensions.getTriggerTooltipText
import liltojustice.trueadaptivemusic.client.gui.widget.utility.*
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.trigger.event.ErrorEvent
import liltojustice.trueadaptivemusic.client.music.tree.MusicTree
import net.minecraft.client.gui.Click
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Colors
import java.util.Timer
import kotlin.concurrent.schedule
import kotlin.reflect.full.primaryConstructor

class NodeViewWidget(
    width: Int,
    height: Int,
    private val musicPack: MusicPack,
    private val onChangesSaved: (newTarget: MusicTree.Node?) -> Unit,
    private val onEventClick: (event: MusicEvent?) -> Unit,
    private val inEventView: () -> Boolean,
    x: Int = 0,
    y: Int = 0)
    : ContainerWidget(
    width,
    height,
    Text.translatableWithFallback(
        "trueadaptivemusic.node_view", "Node View").string,
    true,
    false,
    true,
    true,
    x,
    y) {
    private val defaultNodeParams = MusicTree.Node.Parameters.default().getTriggerParams().map { it.value }
    private val requiredNodeParams = MusicTree.Node.Parameters::class.primaryConstructor?.parameters ?: listOf()
    private var newNodeParent: MusicTree.Node? = null
    private var nodeParams: MutableList<Any?> = defaultNodeParams.toMutableList()
    private var events = mutableListOf<MusicEvent>()
    private var selectedEvent: MusicEvent? = null
    private var selectedNode: MusicTree.Node? = null
    private var selectedMusicPaths = mutableListOf<String>()
    private var selectedAmbiencePaths = mutableListOf<String>()
    private var soundLibrary = musicPack.getEditPackSoundLibrary()

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    override fun mouseClicked(click: Click, doubled: Boolean): Boolean {
        if (isMouseOver(click.x, click.y)) {
            screen?.focused = null
        }

        return super.mouseClicked(click, doubled)
    }

    override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderWidget(context, mouseX, mouseY, delta)
        if (!visible) {
            return
        }

        if (selectedNode != null || newNodeParent != null) {
            renderEditMode()
        }
        else {
            drawCenteredText(
                context,
                Text.translatableWithFallback(
                    "trueadaptivemusic.select_add_node", "Select or create a node").string,
                0,
                width / 2)
        }
    }

    fun renderEditMode() {
        addWidgetFromRender(
            {
                MultiSelectDropdownWidget(
                    listOf(),
                    width,
                    null,
                    { selected ->
                        selectedMusicPaths = selected.toMutableList()
                        onChange()
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
                        TAMClient.playSoundNow(option?.let { PlayableSound.of(it, soundLibrary) }) },
                    tooltipText = Text.translatableWithFallback(
                        "trueadaptivemusic.music_choice.description",
                        "Select any amount of music to be chosen randomly to play"
                    )
                )
            },
            "musicChoice"
        )

        addWidgetFromRender(
            {
                MultiSelectDropdownWidget(
                    listOf(),
                    width,
                    null,
                    { selected ->
                        selectedAmbiencePaths = selected.toMutableList()
                        onChange()
                    },
                    Text.translatableWithFallback(
                        "trueadaptivemusic.ambience_choice", "Ambience Choice").string,
                    {
                        musicPack.getEditPackSoundLibrary().map { (assetName, _) -> assetName }.toMutableSet()
                            .union(
                                Registries.SOUND_EVENT.ids
                                    .map { id -> id.toString() }
                                    .filter { path -> path.contains("music.") }
                            )
                            .toList()
                    },
                    Text.translatableWithFallback(
                        "trueadaptivemusic.select_track", "Select tracks").string,
                    selectedAmbiencePaths,
                    onHoverOption = { option ->
                        TAMClient.playSoundNow(option?.let { PlayableSound.of(it, soundLibrary) }) },
                    tooltipText = Text.translatableWithFallback(
                        "trueadaptivemusic.ambience_choice.description",
                        "Select any amount of ambience to be chosen randomly to play")
                )
            },
            "ambienceChoice"
        )

        requiredNodeParams.forEach { param ->
            addWidgetFromRender(
                {
                    TAMClient.makeInputWidget(
                        screen!!,
                        nodeParams,
                        param,
                        param.name?.let { MusicTree.Node.Parameters.getParamDisplayName(it) },
                        param.name?.let { MusicTree.Node.Parameters.getParamDescription(it) }
                    ) { onChange() }
                },
                "nodeParams: ${param.name ?: param.index}"
            )
        }

        addWidgetFromRender({ EmptyClickableWidget() }, "eventsSpacer")

        addWidgetFromRender(
            {
                val newWidget = ClickableTextWidget(
                    "${Text.translatableWithFallback("trueadaptivemusic.events", "Events").string}:")
                newWidget.active = false
                newWidget
            }, "events"
        )

        events.forEach { event ->
            addWidgetFromRender(
                { val eventWidget = ClickableTextWidget(
                    MusicEvent.getDisplayName(event.getTypeName()).string,
                    onClick = {
                        if (selectedEvent === event) {
                            return@ClickableTextWidget
                        }

                        selectedEvent = event
                        onEventClick(event)
                        scrollToBottom() },
                    isSelected = { selectedEvent == event })
                    eventWidget.setTooltip(Tooltip.of(event.getTriggerTooltipText()))
                    if (event is ErrorEvent) {
                        eventWidget.color = Colors.RED
                    }

                    eventWidget
                },
                "event: ${event.hashCode()}")
        }

        addWidgetFromRender(
            {
                val result = ClickableTextWidget(
                    "+ ${
                        Text.translatableWithFallback(
                            "trueadaptivemusic.create_event", "Create Event").string}",
                    onClick = {
                        selectedEvent = null
                        onEventClick(null)
                        scrollToBottom() },
                    isSelected = { selectedEvent == null && inEventView() }
                )
                result.setTooltip(
                    Tooltip.of(
                        Text.translatableWithFallback(
                            "trueadaptivemusic.create_event", "Create a new event")
                    )
                )
                result
            },
            "Add Event")

        addWidgetFromRender(
            {
                EmptyClickableWidget()
            },
            "deleteSpacer"
        )

        if (selectedNode?.parent != null) {
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

                            selectedNode?.orphan()
                            save(true)
                        }
                    )
                },
                "Delete"
            )
            result.setTooltip(
                Tooltip.of(
                    Text.translatableWithFallback(
                        "trueadaptivemusic.delete_predicate_description", "Delete this predicate")
                )
            )
        }
    }

    fun setEditExistingNode(node: MusicTree.Node) {
        clearWidgetsFromRender()
        selectedNode = node
        selectedMusicPaths = node.music.map { sound -> sound.getSoundName() }.toMutableList()
        selectedAmbiencePaths = node.ambience.map { sound -> sound.getSoundName() }.toMutableList()
        nodeParams = node.parameters.getTriggerParams().map { param -> param.value }.toMutableList()
        events = node.events.toMutableList()
        resetScrolling()
    }

    fun setCreateNewNode(parent: MusicTree.Node) {
        clearWidgetsFromRender()
        newNodeParent = parent
        selectedNode = null
        selectedMusicPaths = mutableListOf()
        selectedAmbiencePaths = mutableListOf()
        nodeParams = defaultNodeParams.toMutableList()
        events = mutableListOf()
        resetScrolling()
    }

    fun onEventModeSave(newEvent: MusicEvent?, exit: Boolean) {
        newEvent?.let {
            events.remove(selectedEvent)
            events.add(it)
        }

        events.sortBy { event -> event.getTriggerId() }
        clearWidgetsFromRender { widget -> !widget.id.startsWith("event:") }

        if (exit && newEvent == null) {
            events.remove(selectedEvent)
        }

        selectedEvent = if (exit) {
            null
        } else {
            newEvent
        }

        selectedNode?.events = events
        save()
    }

    fun reset() {
        clearWidgetsFromRender { false }
        selectedNode = null
        newNodeParent = null
    }

    private fun save(exit: Boolean = false) {
        musicPack.initRules()

        if (exit) {
            selectedNode = null
            newNodeParent = null
            clearWidgetsFromRender { false }
        }

        onChangesSaved(selectedNode)
    }

    private fun onChange() {
        if (selectedNode == null && nodeParams.filterNotNull().size == requiredNodeParams.size) {
            selectedNode = makeNewChild()
        }

        if (nodeParams.filterNotNull().size != requiredNodeParams.size) {
            return
        }

        soundLibrary = musicPack.getEditPackSoundLibrary()
        selectedNode?.let { node ->
            node.music = selectedMusicPaths.mapNotNull { path -> PlayableSound.of(path, soundLibrary) }
            node.ambience = selectedAmbiencePaths.mapNotNull { path -> PlayableSound.of(path, soundLibrary) }
            node.events = events.toList()
            node.parameters = MusicTree.Node.Parameters.fromArgs(nodeParams.filterNotNull())
        }

        save()
    }

    private fun makeNewChild(): MusicTree.Node? {
        return newNodeParent?.newChild(
            nodeParams.filterNotNull(),
            events,
            selectedMusicPaths.mapNotNull {
                    path -> PlayableSound.of(path, soundLibrary) },
            selectedAmbiencePaths.mapNotNull {
                    path -> PlayableSound.of(path, soundLibrary) }
        )
    }
}
