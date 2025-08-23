package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.extensions.getTriggerTooltipText
import liltojustice.trueadaptivemusic.client.gui.widget.utility.*
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.music.MusicPack
import liltojustice.trueadaptivemusic.client.trigger.event.ErrorEvent
import liltojustice.trueadaptivemusic.client.trigger.predicate.ErrorPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicateTree
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.RootPredicate
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Colors
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class PredicateViewWidget(
    width: Int,
    height: Int,
    private val musicPack: MusicPack,
    private val onChangesSaved: () -> Unit,
    private val onEventClick: (event: MusicEvent?) -> Unit,
    private val inEventView: () -> Boolean,
    x: Int = 0,
    y: Int = 0)
    : ContainerWidget(
    width, height, "Predicate View", true, false, true, true, x, y) {
    private val predicateTypeNameOptions = TAMClient.predicateRegistry.getAllNames()
        .filter { typeName -> typeName != TAMClient.predicateRegistry[RootPredicate::class] }
    private var selectedPredicateTypeName: String = predicateTypeNameOptions.firstOrNull() ?: ""
    private var requiredPredicateArgs = listOf<KParameter>()
    private var predicateArgs = mutableListOf<Any?>()
    private val requiredNodeArgs = MusicPredicateTree.Node.Parameters::class.primaryConstructor?.parameters ?: listOf()
    private var nodeArgs: MutableList<Any?> = requiredNodeArgs.map { null }.toMutableList()
    private var events = mutableListOf<MusicEvent>()
    private var selectedEvent: MusicEvent? = null
    private var selectedNode: MusicPredicateTree.Node? = null
    private var newPredicateParent: MusicPredicateTree.Node? = null
    private var selectedMusicPaths = mutableListOf<String>()
    private var assets = musicPack.getEditPackAssets()

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

        if (newPredicateParent != null || selectedNode != null) {
            renderEditMode(mouseX, mouseY)
        }
        else {
            drawCenteredText(
                context,
                "Select or add a predicate",
                0,
                width / 2)
        }
    }

    fun setEditExistingNode(node: MusicPredicateTree.Node) {
        clearWidgetsFromRender()
        setSelectedPredicateTypeName(node.predicate.getTypeName())
        selectedNode = node
        selectedMusicPaths = selectedNode!!.predicate.playableSounds.map { sound -> sound.getSoundName() }
            .toMutableList()
        newPredicateParent = null
        nodeArgs = node.parameters.constructorParams().toMutableList()
        events = node.events.toMutableList()
        resetScrolling()
    }

    fun setCreateNewNode(parent: MusicPredicateTree.Node) {
        clearWidgetsFromRender()
        selectedPredicateTypeName = ""
        selectedNode = null
        selectedMusicPaths = mutableListOf()
        newPredicateParent = parent
        requiredPredicateArgs = listOf()
        predicateArgs = mutableListOf()
        nodeArgs.replaceAll { null }
        events = mutableListOf()
        resetScrolling()
    }

    fun onEventModeExit(newEvent: MusicEvent?) {
        events.remove(selectedEvent)
        newEvent?.let { events.add(it) }
        selectedEvent = null
        clearWidgetsFromRender()
    }

    private fun setSelectedPredicateTypeName(typeName: String) {
        selectedPredicateTypeName = typeName
        requiredPredicateArgs = TAMClient.predicateFactory.getRequiredArgs(typeName)
        predicateArgs = selectedNode?.let {
            if (it.predicate.getTypeName() == selectedPredicateTypeName)
                it.predicate.getTriggerParams().map { param -> param.value }.toMutableList()
            else
                null
        } ?: requiredPredicateArgs.map { null }.toMutableList()

        clearWidgetsFromRender { childWidget -> childWidget.id in arrayOf("predicateTypeChoice", "musicChoice") }
    }

    private fun renderEditMode(mouseX: Int, mouseY: Int) {
        if (selectedNode?.predicate is ErrorPredicate) {
            renderErrorMode()
            return
        }

        if (selectedNode?.predicate !is RootPredicate) {
            addWidgetFromRender(
                {
                    DropdownWidget(
                        predicateTypeNameOptions,
                        { typeName ->  setSelectedPredicateTypeName(typeName) },
                        width,
                        "Type",
                        startingOption = selectedPredicateTypeName)
                },
                "predicateTypeChoice",
                row = 1)
        }

        val musicDropdownWidget = addWidgetFromRender(
            {
                MultiSelectDropdownWidget(
                    listOf(),
                    width,
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

        requiredPredicateArgs.forEach { arg ->
            addWidgetFromRender(
                { TAMClient.makeInputWidget(screen!!, predicateArgs, arg) },
                "predicateArg: ${arg.name ?: arg.index}"
            )
        }

        requiredNodeArgs.forEach { arg ->
            addWidgetFromRender(
                { TAMClient.makeInputWidget(screen!!, nodeArgs, arg) },
                "nodeArg: ${arg.name ?: arg.index}"
            )
        }

        addWidgetFromRender({ EmptyClickableWidget() }, "empty")

        addWidgetFromRender({
            val newWidget = ClickableTextWidget("Events:")
            newWidget.active = false
            newWidget
        }, "events")

        events.forEach { event ->
            addWidgetFromRender(
                { val eventWidget = ClickableTextWidget(
                    event.getTypeName(),
                    onClick = {
                        if (selectedEvent === event) {
                            return@ClickableTextWidget
                        }

                        selectedEvent = event
                        onEventClick(event) },
                    isSelected = { selectedEvent == event })
                    eventWidget.tooltip = Tooltip.of(event.getTriggerTooltipText())
                    if (event is ErrorEvent) {
                        eventWidget.color = Colors.RED
                    }

                    eventWidget
                },
                "event: ${event.hashCode()}")
        }

        addWidgetFromRender(
            { ClickableTextWidget(
                "+ Add",
                onClick = {
                    selectedEvent = null
                    onEventClick(null) },
                isSelected = { selectedEvent == null && inEventView() }) },
            "Add Event")

        addWidgetFromRender({ EmptyClickableWidget() }, "empty")

        val saveWidget = addWidgetFromRender(
            {
                ClickableTextWidget(
                    "Save",
                    onClick = {
                        assets = musicPack.getEditPackAssets()
                        if (selectedNode != null) {
                            selectedNode!!.predicate =
                                if (selectedNode!!.predicate.getTypeName()
                                    == TAMClient.predicateRegistry[RootPredicate::class])
                                    selectedNode!!.predicate
                                else TAMClient.predicateFactory.fromArgs(
                                    selectedPredicateTypeName,
                                    selectedMusicPaths
                                        .mapNotNull { path -> MusicPack.toPlayableSound(assets, path) },
                                    *predicateArgs.filterNotNull().toTypedArray())
                            selectedNode!!.events = events
                            selectedNode!!.parameters =
                                MusicPredicateTree.Node.Parameters.initializeFromArgs(
                                    *nodeArgs.filterNotNull().toTypedArray())
                        }
                        else {
                            newPredicateParent?.newChild(
                                selectedPredicateTypeName,
                                nodeArgs.filterNotNull(),
                                predicateArgs.filterNotNull(),
                                events,
                                selectedMusicPaths.mapNotNull { path -> MusicPack.toPlayableSound(assets, path) })
                        }

                        save()
                    })
            },
            "Save"
        ) as ClickableTextWidget

        if (selectedNode?.parent != null) {
            addWidgetFromRender(
                {
                    ClickableTextWidget(
                        "Delete",
                        onClick = {
                            selectedNode?.orphan()
                            save()
                        }
                    )
                },
                "Delete"
            )
        }

        saveWidget.active = predicateArgs.filterNotNull().size == requiredPredicateArgs.size
        saveWidget.color = if (saveWidget.active) Colors.WHITE else Colors.RED
        saveWidget.tooltip =
            if (saveWidget.active)
                null
            else
                MISSING_ARGS_TOOLTIP
    }

    private fun renderErrorMode() {
        addWidgetFromRender(
            {
                ClickableTextWidget(
                    "Delete",
                    onClick = {
                        selectedNode?.orphan()
                        save()
                    }
                )
            },
            "Delete"
        )
    }

    private fun unsetAll() {
        clearWidgetsFromRender()
        newPredicateParent = null
        selectedNode = null
    }

    private fun save() {
        musicPack.initRules()
        onChangesSaved()
        unsetAll()
    }

    companion object {
        private val MISSING_ARGS_TOOLTIP =
            Tooltip.of(Text.literal("At least one required parameter for this type is missing."))
    }
}
