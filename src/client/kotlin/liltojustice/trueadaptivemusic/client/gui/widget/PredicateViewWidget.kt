package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.extensions.getTriggerTooltipText
import liltojustice.trueadaptivemusic.client.gui.widget.utility.*
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.music.MusicPack
import liltojustice.trueadaptivemusic.client.trigger.event.ErrorEvent
import liltojustice.trueadaptivemusic.client.trigger.predicate.ErrorPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicateTree
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.RootPredicate
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.TextWidget
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Colors
import java.util.Timer
import kotlin.concurrent.schedule
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class PredicateViewWidget(
    width: Int,
    height: Int,
    private val musicPack: MusicPack,
    private val onChangesSaved: (target: MusicPredicateTree.Node?) -> Unit,
    private val onEventClick: (event: MusicEvent?) -> Unit,
    private val inEventView: () -> Boolean,
    x: Int = 0,
    y: Int = 0)
    : ContainerWidget(
    width,
    height,
    Text.translatableWithFallback(
        "trueadaptivemusic.predicate_view", "Predicate View").string,
    true,
    false,
    true,
    true,
    x,
    y) {
    private val predicateTypeNameOptions = TAMClient.predicateRegistry.getAllNames()
        .filter { typeName -> typeName != TAMClient.predicateRegistry[RootPredicate::class] }
    private var selectedPredicateTypeName: String = predicateTypeNameOptions.firstOrNull() ?: ""
    private var requiredPredicateArgs = listOf<KParameter>()
    private var predicateArgs = mutableListOf<Any?>()
    private val requiredPredicateParams = MusicPredicate.Parameters::class.primaryConstructor?.parameters ?: listOf()
    private var predicateParams: MutableList<Any?> = requiredPredicateParams.map { null }.toMutableList()
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

    override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderWidget(context, mouseX, mouseY, delta)
        if (!visible) {
            return
        }

        if (newPredicateParent != null || selectedNode != null) {
            renderEditMode(mouseX, mouseY)
        }
        else {
            drawCenteredText(
                context,
                Text.translatableWithFallback(
                    "trueadaptivemusic.select_add_predicate", "Select or add a predicate").string,
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
        predicateParams = node.predicate.parameters.constructorParams().toMutableList()
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
        predicateParams.replaceAll { null }
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
        save()

        if (exit && newEvent == null) {
            events.remove(selectedEvent)
        }

        selectedEvent = if (exit) {
            null
        } else {
            newEvent
        }
    }

    private fun setSelectedPredicateTypeName(typeName: String) {
        selectedPredicateTypeName = typeName
        requiredPredicateArgs = TAMClient.predicateFactory.getRequiredArgs(typeName)
        predicateArgs = selectedNode?.let {
            if (it.predicate.getTypeName() == selectedPredicateTypeName)
                it.predicate.getTriggerArgs().map { arg -> arg.value }.toMutableList()
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
                        { typeName ->
                            setSelectedPredicateTypeName(typeName)
                            if (selectedNode == null
                                && predicateArgs.filterNotNull().size == requiredPredicateArgs.size
                                && predicateParams.filterNotNull().size == requiredPredicateParams.size) {
                                selectedNode = newPredicateParent?.newChild(
                                    selectedPredicateTypeName,
                                    predicateParams.filterNotNull(),
                                    predicateArgs.filterNotNull(),
                                    events,
                                    selectedMusicPaths.mapNotNull {
                                        path -> MusicPack.toPlayableSound(assets, path) })
                            }
                        },
                        width,
                        Text.translatableWithFallback("trueadaptivemusic.type", "Type").string,
                        startingOption = selectedPredicateTypeName)
                },
                "predicateTypeChoice",
                row = 1)
        }
        else {
            addWidgetFromRender(
                { TextWidget(Text.literal("root"), textRenderer) }, "root", row = 1)
        }

        val musicDropdownWidget = addWidgetFromRender(
            {
                MultiSelectDropdownWidget(
                    listOf(),
                    width,
                    { selected ->
                        selectedMusicPaths = selected.toMutableList()
                        onChange()
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
                        TAMClient.playSoundNow(option?.let { MusicPack.toPlayableSound(assets, it) }) })
            },
            "musicChoice"
        )

        if (isMouseOver(mouseX.toDouble(), mouseY.toDouble())
            && !musicDropdownWidget.isMouseOver(mouseX.toDouble(), mouseY.toDouble())) {
            TAMClient.playSoundNow(null)
        }

        requiredPredicateArgs.forEach { arg ->
            addWidgetFromRender(
                {
                    TAMClient.makeInputWidget(screen!!, predicateArgs, arg) { onChange() }
                },
                "predicateArg: ${arg.name ?: arg.index}"
            )
        }

        requiredPredicateParams.forEach { param ->
            addWidgetFromRender(
                {
                    TAMClient.makeInputWidget(
                        screen!!, predicateParams, param
                    ) { onChange() }
                },
                "predicateParam: ${param.name ?: param.index}"
            )
        }

        addWidgetFromRender({ EmptyClickableWidget() }, "empty")

        addWidgetFromRender({
            val newWidget = ClickableTextWidget(
                "${Text.translatableWithFallback("trueadaptivemusic.events", "Events").string}:")
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
                "+ ${Text.translatableWithFallback("trueadaptivemusic.add", "Add").string}",
                onClick = {
                    selectedEvent = null
                    onEventClick(null) },
                isSelected = { selectedEvent == null && inEventView() }) },
            "Add Event")

        if (selectedNode?.parent != null) {
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

                            selectedNode?.orphan()
                            save(true)
                        }
                    )
                },
                "Delete"
            )
        }
    }

    private fun renderErrorMode() {
        addWidgetFromRender(
            {
                ClickableTextWidget(
                    Text.translatableWithFallback("trueadaptivemusic.delete", "Delete").string,
                    onClick = {
                        selectedNode?.orphan()
                        save(true)
                    }
                )
            },
            "Delete"
        )
    }

    private fun save(exit: Boolean = false) {
        musicPack.initRules()

        if (exit) {
            selectedNode = null
            newPredicateParent = null
            clearWidgetsFromRender { false }
        }
        onChangesSaved(selectedNode)
    }

    private fun onChange() {
        if (selectedNode == null
            && predicateArgs.filterNotNull().size == requiredPredicateArgs.size
            && predicateParams.filterNotNull().size == requiredPredicateParams.size) {
            selectedNode = newPredicateParent?.newChild(
                selectedPredicateTypeName,
                predicateParams.filterNotNull(),
                predicateArgs.filterNotNull(),
                events,
                selectedMusicPaths.mapNotNull { path -> MusicPack.toPlayableSound(assets, path) })
        }

        if (predicateArgs.filterNotNull().size != requiredPredicateArgs.size
            || predicateParams.filterNotNull().size != requiredPredicateParams.size) {
            return
        }

        assets = musicPack.getEditPackAssets()
        selectedNode!!.predicate =
            TAMClient.predicateFactory.fromArgs(
                selectedPredicateTypeName,
                selectedMusicPaths
                    .mapNotNull { path -> MusicPack.toPlayableSound(assets, path) },
                predicateParams.filterNotNull(), predicateArgs.filterNotNull())
        selectedNode!!.events = events

        save()
    }
}
