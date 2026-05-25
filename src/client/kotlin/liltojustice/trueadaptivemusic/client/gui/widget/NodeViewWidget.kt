package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.extensions.getTriggerId
import liltojustice.trueadaptivemusic.client.gui.extensions.getTriggerTooltipText
import liltojustice.trueadaptivemusic.client.gui.widget.utility.*
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.music.tree.MusicTree
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSoundDirectory
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSoundFile
import liltojustice.trueadaptivemusic.client.trigger.event.ErrorEvent
import liltojustice.trueadaptivemusicapi.TAMAPI
import liltojustice.trueadaptivemusicapi.widget.EmptyClickableWidget
import liltojustice.trueadaptivemusicapi.widget.WidgetArg
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.util.CommonColors
import java.util.Timer
import kotlin.collections.toMutableList
import kotlin.concurrent.schedule
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.typeOf

class NodeViewWidget(
    width: Int,
    height: Int,
    private val musicPack: MusicPack,
    private val onChangesSaved: (newTarget: MusicTree.Node?) -> Unit,
    private val onEventClick: (event: MusicEvent<*>?) -> Unit,
    private val inEventView: () -> Boolean,
    x: Int = 0,
    y: Int = 0
): ContainerWidget(
    width,
    height,
    Component.translatableWithFallback("trueadaptivemusic.node_view", "Node View").string,
    true,
    false,
    true,
    false,
    true,
    x,
    y
) {
    private val defaultNodeParams = MusicTree.Node.Parameters.default().getMusicParams().map { it.value }
    private val requiredNodeParams = MusicTree.Node.Parameters::class.primaryConstructor?.parameters
        ?.map { WidgetArg.of(it) } ?: listOf()
    private var newNodeParent: MusicTree.Node? = null
    private var nodeParams: MutableList<Any?> = defaultNodeParams.toMutableList()
    private var events = mutableListOf<MusicEvent<*>>()
    private var selectedEvent: MusicEvent<*>? = null
    private var selectedNode: MusicTree.Node? = null
    private var selectedMusicPaths = mutableListOf<String>()
    private var selectedAmbiencePaths = mutableListOf<String>()
    private var soundLibrary = musicPack.getEditPackSoundLibrary()
    private var shouldSave = false
    private var shouldExit = false
    private var lastRestricted = false
    private val restrictedParameters: Set<String>
        get() = run {
            val node = selectedNode ?: return emptySet<String>()
            val result = mutableSetOf<String>()

            if (!musicPack.options.persistentNodeMusic) {
                result += MusicTree.Node.Parameters::ignorePersistence.name
            }

            if (node.parameters.vanillaMusic) {
                result += listOf(
                    MusicTree.Node.Parameters::inheritMusic.name,
                    MusicTree.Node.Parameters::parallelMusic.name,
                    MusicTree.Node.Parameters::loopMusic.name,
                    MusicTree.Node.Parameters::loopStartPoints.name,
                )
            }
            else {
                result += MusicTree.Node.Parameters::compatibilityMode.name
            }

            if (node.parameters.parallelMusic) {
                result += listOf(
                    MusicTree.Node.Parameters::ignorePersistence.name,
                    MusicTree.Node.Parameters::vanillaMusic.name,
                    MusicTree.Node.Parameters::trackDelay.name,
                    MusicTree.Node.Parameters::trackDelayNoise.name,
                    MusicTree.Node.Parameters::enterDelay.name,
                    MusicTree.Node.Parameters::inheritMusic.name,
                    MusicTree.Node.Parameters::loopMusic.name,
                )
            }

            if (node.parameters.loopMusic) {
                result += MusicTree.Node.Parameters::ignorePersistence.name
            }

            if (node.parent?.parameters?.parallelMusic == true) {
                result += listOf(
                    MusicTree.Node.Parameters::vanillaMusic.name,
                    MusicTree.Node.Parameters::parallelMusic.name,
                    MusicTree.Node.Parameters::trackDelay.name,
                    MusicTree.Node.Parameters::trackDelayNoise.name,
                    MusicTree.Node.Parameters::enterDelay.name,
                    MusicTree.Node.Parameters::inheritMusic.name,
                    MusicTree.Node.Parameters::loopMusic.name,
                    MusicTree.Node.Parameters::loopStartPoints.name,
                )
            }

            if (node.parent == null) {
                result += listOf(
                    MusicTree.Node.Parameters::inheritMusic.name,
                    MusicTree.Node.Parameters::inheritAmbience.name
                )
            }

            result.toSet()
        }

    override fun updateWidgetNarration(output: NarrationElementOutput) {
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        if (isMouseOver(event.x, event.y)) {
            screen?.focused = null
        }

        return super.mouseClicked(event, doubleClick)
    }

    override fun extractWidgetRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, a)
        if (!visible) {
            return
        }

        if (selectedNode != null || newNodeParent != null) {
            renderEditMode()
        }
        else {
            drawCenteredText(
                graphics,
                Component.translatableWithFallback(
                    "trueadaptivemusic.select_add_node", "Select or create a node").string,
                0,
                width / 2)
        }

        if (shouldSave) {
            save(shouldExit)
        }

        shouldExit = false
        shouldSave = false
    }

    fun renderEditMode() {
        val restricted = selectedNode?.let { enforceParameterConstraints(musicPack, it) } ?: false
        if (restricted) {
            clearRestrictedWidgets()
        }

        if (restricted != lastRestricted) {
            clearWidgetsFromRender { widget -> widget.id != "musicChoice" }
        }

        lastRestricted = restricted

        if (selectedNode?.parameters?.vanillaMusic == false) {
            addWidgetFromRender(
                {
                    if (restricted && selectedNode?.parameters?.parallelMusic == true) {
                        DropdownWidget(
                            listOf(),
                            { selected ->
                                selectedMusicPaths = mutableListOf(selected)
                                clearLoopIntroEndpointWidgets()
                                onChange()
                            },
                            width,
                            Component.translatableWithFallback(
                                "trueadaptivemusic.music_choice", "Music Choice").string,
                            getOptions = {
                                musicPack.getEditPackSoundLibrary().map { (assetName, _) -> assetName }.toMutableSet()
                                    .union(
                                        BuiltInRegistries.SOUND_EVENT.keySet()
                                            .map { id -> id.toString() }
                                            .filter { path -> path.contains("music.") }
                                    )
                                    .sorted()
                            },
                            notSelectedPlaceholder = selectedMusicPaths.firstOrNull()
                                ?: Component
                                    .translatableWithFallback(
                                        "trueadaptivemusic.select_track", "Select tracks").string,
                            onHoverOption = { option ->
                                TAMClient.playSoundNow(option?.let { PlayableSound.of(it, soundLibrary) })
                            },
                            tooltipText = Component.translatableWithFallback(
                                "trueadaptivemusic.music_choice.description",
                                "Select any amount of music to be chosen randomly to play"
                            )
                        )
                    }
                    else {
                        MultiSelectDropdownWidget(
                            listOf(),
                            width,
                            null,
                            { selected ->
                                selectedMusicPaths = selected.toMutableList()
                                clearLoopIntroEndpointWidgets()
                                onChange()
                            },
                            Component.translatableWithFallback(
                                "trueadaptivemusic.music_choice", "Music Choice"
                            ).string,
                            {
                                musicPack.getEditPackSoundLibrary().map { (assetName, _) -> assetName }.toMutableSet()
                                    .union(
                                        BuiltInRegistries.SOUND_EVENT.keySet()
                                            .map { id -> id.toString() }
                                            .filter { path -> path.contains("music.") }
                                    )
                                    .sorted()
                            },
                            Component.translatableWithFallback(
                                "trueadaptivemusic.select_track", "Select tracks"
                            ).string,
                            selectedMusicPaths,
                            { option ->
                                TAMClient.playSoundNow(option?.let { PlayableSound.of(it, soundLibrary) })
                            },
                            Component.translatableWithFallback(
                                "trueadaptivemusic.music_choice.description",
                                "Select any amount of music to be chosen randomly to play"
                            ),
                            customCreator = { text -> Identifier.tryParse(text)?.toString() }
                        )
                    }
                },
                "musicChoice"
            )
        }

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
                    Component.translatableWithFallback(
                        "trueadaptivemusic.ambience_choice", "Ambience Choice").string,
                    {
                        musicPack.getEditPackSoundLibrary().map { (assetName, _) -> assetName }.toMutableSet()
                            .union(
                                BuiltInRegistries.SOUND_EVENT.keySet()
                                    .map { id -> id.toString() }
                                    .filter { path -> path.contains("music.") }
                            )
                            .toList()
                    },
                    Component.translatableWithFallback(
                        "trueadaptivemusic.select_track", "Select tracks").string,
                    selectedAmbiencePaths,
                    { option ->
                        TAMClient.playSoundNow(option?.let { PlayableSound.of(it, soundLibrary) }) },
                    Component.translatableWithFallback(
                        "trueadaptivemusic.ambience_choice.description",
                        "Select any amount of ambience to be chosen randomly to play"),
                    customCreator = { text -> Identifier.tryParse(text)?.toString() }
                )
            },
            "ambienceChoice"
        )

        requiredNodeParams.dropLast(1).filter { it.name !in restrictedParameters }.forEach { param ->
            addWidgetFromRender(
                {
                    TAMAPI.makeInputWidget(
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

        selectedNode?.let { node ->
            if (!node.parameters.loopMusic || "loopStartPoints" in restrictedParameters) {
                clearLoopIntroEndpointWidgets()
                return@let
            }

            val loopStartPointsParam = requiredNodeParams.last()
            if (node.parameters.parallelMusic) {
                clearLoopIntroEndpointWidgets()
                addWidgetFromRender(
                    {
                        val outArg = mutableListOf(node.parameters.loopStartPoints.values.firstOrNull() as Any?)
                        TAMAPI.makeInputWidget(
                            screen!!,
                            outArg,
                            WidgetArg(typeOf<UInt>(), "loopStartPoints", 0),
                            Component.literal("Parallel loop start point"),
                            null
                        ) {
                            nodeParams[loopStartPointsParam.index] = mapOf("parallel" to outArg[0] as UInt)
                            onChange()
                        }
                    },
                    "loopStartPoint: parallel"
                )

                return@let
            }

            addWidgetFromRender({ EmptyClickableWidget() }, "loopStartPointsSpacer")
            addWidgetFromRender(
                {
                    val newWidget = ClickableTextWidget(
                        "${
                            Component.translatableWithFallback(
                            "trueadaptivemusic.loop_start_points", "Loop Start Points").string}:"
                    )
                    newWidget.active = false
                    newWidget.setTooltip(
                        Tooltip.create(MusicTree.Node.Parameters.getParamDescription("loopStartPoints")))
                    newWidget
                }, "loopStartPoints"
            )

            val soundNames = node.music
                .filter { it is PlayableSoundFile || it is PlayableSoundDirectory }
                .flatMap { sound ->
                    (sound as? PlayableSoundFile)?.let { listOf(it.getSoundName()) }
                        ?: (sound as? PlayableSoundDirectory)
                            ?.getInteriorSounds(soundLibrary)?.map { it.getSoundName() }
                        ?: emptyList()
                }

            queueClearWidgetsFromRender { it.id != "loopStartPoint: parallel" }

            soundNames.sorted().forEach { soundName ->
                addWidgetFromRender(
                    {
                        val outArg = mutableListOf(node.parameters.loopStartPoints[soundName] as Any?)
                        TAMAPI.makeInputWidget(
                            screen!!,
                            outArg,
                            WidgetArg(typeOf<UInt>(), "loopStartPoints", 0),
                            Component.literal(soundName),
                            null
                        ) {
                            val copy = mutableMapOf<String, UInt>()
                            soundNames.forEach { copy[it] = 0U }
                            node.parameters.loopStartPoints.entries.forEach { entry ->
                                if (entry.key in copy) {
                                    copy[entry.key] = entry.value
                                }
                            }

                            copy[soundName] = outArg[0] as UInt
                            nodeParams[loopStartPointsParam.index] = copy.toMap()
                            onChange()
                        }
                    },
                    "loopStartPoints: $soundName"
                )
            }
        }

        addWidgetFromRender({ EmptyClickableWidget() }, "eventsSpacer")

        addWidgetFromRender(
            {
                val newWidget = ClickableTextWidget(
                    "${Component.translatableWithFallback(
                        "trueadaptivemusic.events", "Events").string}:"
                )
                newWidget.active = false
                newWidget
            }, "events"
        )

        events.forEach { event ->
            addWidgetFromRender(
                { val eventWidget = ClickableTextWidget(
                    MusicEvent.getDisplayName(event.type.typeName).string,
                    onClick = {
                        if (selectedEvent === event) {
                            return@ClickableTextWidget
                        }

                        selectedEvent = event
                        onEventClick(event)
                        scrollToBottom() },
                    isSelected = { selectedEvent == event })
                    eventWidget.setTooltip(Tooltip.create(event.getTriggerTooltipText()))
                    if (event.type is ErrorEvent) {
                        eventWidget.color = CommonColors.RED
                    }

                    eventWidget
                },
                "event: ${event.hashCode()}")
        }

        addWidgetFromRender(
            {
                val result = ClickableTextWidget(
                    "+ ${
                        Component.translatableWithFallback(
                            "trueadaptivemusic.create_event", "Create Event").string}",
                    onClick = {
                        selectedEvent = null
                        onEventClick(null)
                        scrollToBottom() },
                    isSelected = { selectedEvent == null && inEventView() }
                )
                result.setTooltip(
                    Tooltip.create(
                        Component.translatableWithFallback(
                            "trueadaptivemusic.create_event", "Create a new event")
                    )
                )
                result
            },
            "Add Event"
        )

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
                        Component.translatableWithFallback("trueadaptivemusic.delete", "Delete")
                            .string,
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
                                            "trueadaptivemusic.delete", "Delete").string
                                    )
                                    widget.color = CommonColors.WHITE
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
                Tooltip.create(
                    Component.translatableWithFallback(
                        "trueadaptivemusic.delete_node_description", "Delete this node")
                )
            )
        }

        addWidgetFromRender(
            {
                EmptyClickableWidget()
            },
            "finalSpacer"
        )
    }

    fun setEditExistingNode(node: MusicTree.Node) {
        clearWidgetsFromRender()
        selectedNode = node
        selectedEvent = null
        selectedMusicPaths = node.music.map { sound -> sound.getSoundName() }.toMutableList()
        selectedAmbiencePaths = node.ambience.map { sound -> sound.getSoundName() }.toMutableList()
        nodeParams = node.parameters.getMusicParams().map { param -> param.value }.toMutableList()
        events = node.events.toMutableList()
        resetScrolling()
    }

    fun setCreateNewNode(parent: MusicTree.Node) {
        clearWidgetsFromRender()
        newNodeParent = parent
        selectedNode = null
        selectedEvent = null
        selectedMusicPaths = mutableListOf()
        selectedAmbiencePaths = mutableListOf()
        nodeParams = defaultNodeParams.toMutableList()
        events = mutableListOf()
        resetScrolling()
    }

    fun onEventModeSave(newEvent: MusicEvent<*>?, exit: Boolean) {
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
            selectedEvent = null
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

        shouldSave = true
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

    private fun clearLoopIntroEndpointWidgets() {
        queueClearWidgetsFromRender { !it.id.startsWith("loopStartPoint") }
    }

    private fun clearRestrictedWidgets() {
        queueClearWidgetsFromRender { widget -> restrictedParameters.none { widget.id.contains(it) } }
    }

    companion object {
        private fun enforceParameterConstraints(musicPack: MusicPack, node: MusicTree.Node): Boolean {
            node.parent?.let {
                if (it.parameters.parallelMusic) {
                    node.parameters.parallelMusic = true
                }
            }

            if (node.parameters.parallelMusic) {
                node.children.forEach { enforceParameterConstraints(musicPack, it) }
                node.parameters.inheritMusic = false
                node.parameters.trackDelay = 0U
                node.parameters.trackDelayNoise = 0U
                node.parameters.enterDelay = 0U
                node.parameters.loopMusic = true
            }

            return node.parameters.parallelMusic || node.parameters.vanillaMusic || node.parameters.loopMusic
        }
    }
}
