package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.widget.utility.*
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import liltojustice.trueadaptivemusic.client.trigger.predicate.ErrorPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import liltojustice.trueadaptivemusic.client.music.tree.MusicTree
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.RootPredicate
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.text.Text
import net.minecraft.util.Colors
import java.util.Timer
import kotlin.concurrent.schedule

class PredicateViewWidget(
    width: Int,
    height: Int,
    private val musicPack: MusicPack,
    private val onChangesSaved: (targetNode: MusicTree.Node?, targetPredicate: MusicPredicate?, exit: Boolean) -> Unit,
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
    false,
    true,
    x,
    y) {
    private val predicateTypeNameOptions = TAMClient.predicateRegistry.getAllNames()
        .filter { typeName -> typeName != TAMClient.predicateRegistry[RootPredicate::class] }
    private var selectedPredicateTypeName: String = predicateTypeNameOptions.firstOrNull() ?: ""
    private var requiredPredicateArgs = listOf<InputWidgetMaker.WidgetArg>()
    private var predicateArgs = mutableListOf<Any?>()
    private var selectedPredicate: MusicPredicate? = null
    private var selectedNode: MusicTree.Node? = null
    private var soundLibrary = musicPack.getEditPackSoundLibrary()

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

        if (selectedPredicate is ErrorPredicate) {
            renderErrorMode()
            return
        }

        addWidgetFromRender(
            {
                DropdownWidget(
                    predicateTypeNameOptions,
                    { typeName ->
                        setSelectedPredicateTypeName(typeName)
                        if (selectedPredicate == null
                            && predicateArgs.filterNotNull().size == requiredPredicateArgs.size) {
                            selectedPredicate = makeNewPredicate()?.predicates?.lastOrNull()
                        }
                        onChange()
                    },
                    width,
                    Text.translatableWithFallback("trueadaptivemusic.type", "Type").string,
                    { MusicPredicate.getDisplayName(it).string },
                    startingOption = selectedPredicateTypeName.takeIf { it.isNotBlank() },
                    tooltipText = Text.translatableWithFallback(
                        "trueadaptivemusic.predicate_type.description",
                        "Select under what circumstances the music should play"
                    )
                )
            },
            "predicateTypeChoice"
        )

        requiredPredicateArgs.forEach { arg ->
            addWidgetFromRender(
                {
                    TAMClient.makeInputWidget(
                        screen!!,
                        predicateArgs,
                        arg,
                        arg.name
                            ?.let {
                                MusicPredicate.getArgDisplayName(selectedPredicateTypeName, it) },
                        arg.name
                            ?.let {
                                MusicPredicate.getArgDescription(selectedPredicateTypeName, it) }
                    ) { onChange() }
                },
                "predicateArg: ${arg.name ?: arg.index}"
            )
        }

        addWidgetFromRender(
            {
                EmptyClickableWidget()
            },
            "deleteSpacer"
        )

        if (selectedPredicate !is RootPredicate) {
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
                                            "trueadaptivemusic.delete", "Delete").string
                                    )
                                    widget.color = Colors.WHITE
                                }

                                return@ClickableTextWidget
                            }

                            selectedNode?.predicates?.remove(selectedPredicate)
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

        addWidgetFromRender(
            {
                EmptyClickableWidget()
            },
            "finalSpacer"
        )
    }

    fun setEditExistingPredicate(node: MusicTree.Node, predicate: MusicPredicate) {
        clearWidgetsFromRender()
        setSelectedPredicateTypeName(predicate.getTypeName())
        selectedPredicate = predicate
        selectedNode = node
        resetScrolling()
    }

    fun setCreateNewPredicate(parent: MusicTree.Node) {
        clearWidgetsFromRender()
        selectedPredicate = null
        selectedNode = parent
        requiredPredicateArgs = listOf()
        predicateArgs = mutableListOf()
        setSelectedPredicateTypeName(predicateTypeNameOptions.first())
        resetScrolling()
    }

    private fun setSelectedPredicateTypeName(typeName: String) {
        selectedPredicateTypeName = typeName
        requiredPredicateArgs = TAMClient.predicateFactory
            .getRequiredArgs(typeName).map { InputWidgetMaker.WidgetArg.of(it) }
        predicateArgs = selectedPredicate?.let {
            if (it.getTypeName() == selectedPredicateTypeName)
                it.getTriggerArgs().map { arg -> arg.value }.toMutableList()
            else
                null
        } ?: requiredPredicateArgs.map { null }.toMutableList()

        clearWidgetsFromRender { childWidget -> childWidget.id in arrayOf("predicateTypeChoice", "musicChoice") }
    }

    private fun renderErrorMode() {
        val result = addWidgetFromRender(
            {
                ClickableTextWidget(
                    Text.translatableWithFallback("trueadaptivemusic.delete", "Delete").string,
                    onClick = {
                        selectedNode?.predicates?.remove(selectedPredicate)
                        selectedNode = null
                        selectedPredicate = null
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

    private fun save(exit: Boolean = false) {
        musicPack.initRules()

        if (exit) {
            selectedNode = null
            selectedPredicate = null
            clearWidgetsFromRender { false }
        }

        onChangesSaved(selectedNode, selectedPredicate, exit)
    }

    private fun onChange() {
        if (selectedPredicate == null
            && predicateArgs.filterNotNull().size == requiredPredicateArgs.size) {
            selectedNode = makeNewPredicate()
            selectedPredicate = selectedNode?.predicates?.lastOrNull()
        }

        if (predicateArgs.filterNotNull().size != requiredPredicateArgs.size) {
            return
        }

        soundLibrary = musicPack.getEditPackSoundLibrary()
        val replacement =
            TAMClient.predicateFactory.fromArgs(selectedPredicateTypeName, predicateArgs.filterNotNull())
        selectedNode?.let { node ->
            node.predicates = node.predicates.map { predicate ->
                if (predicate == selectedPredicate) {
                    replacement
                }
                else {
                    predicate
                }
            }.toMutableList()
        }

        selectedPredicate = replacement
        save()
    }

    private fun makeNewPredicate(): MusicTree.Node? {
        return selectedNode?.newPredicate(
            selectedPredicateTypeName, predicateArgs.filterNotNull())
    }
}
