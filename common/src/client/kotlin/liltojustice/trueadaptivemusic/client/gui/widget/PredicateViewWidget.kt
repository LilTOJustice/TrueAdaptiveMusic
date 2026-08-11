package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.widget.utility.*
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import liltojustice.trueadaptivemusic.client.music.tree.MusicTreeNode
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import liltojustice.trueadaptivemusic.client.trigger.MusicTriggerException
import liltojustice.trueadaptivemusic.client.trigger.predicate.ErrorPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.RootPredicate
import liltojustice.trueadaptivemusicapi.TAMAPI
import liltojustice.trueadaptivemusicapi.widget.EmptyClickableWidget
import liltojustice.trueadaptivemusicapi.widget.WidgetArg
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import java.util.Timer
import kotlin.concurrent.schedule

class PredicateViewWidget(
    width: Int,
    height: Int,
    private val musicPack: MusicPack,
    private val onChangesSaved:
        (targetNode: MusicTreeNode?, targetPredicate: MusicPredicate<*>?, exit: Boolean) -> Unit,
    x: Int = 0,
    y: Int = 0)
    : ContainerWidget(
    width,
    height,
    Component.translatableWithFallback("trueadaptivemusic.predicate_view", "Predicate View").string,
    true,
    false,
    true,
    false,
    true,
    x,
    y) {
    private val predicateTypeNameOptions = TAMAPI.getPredicateTypeNames().filter {
        typeName -> typeName != Constants.ROOT_PREDICATE_NAME }
    private var selectedPredicateTypeName: String = predicateTypeNameOptions.firstOrNull() ?: ""
    private var requiredPredicateArgs = listOf<WidgetArg>()
    private var predicateArgs = mutableListOf<Any?>()
    private var selectedPredicate: MusicPredicate<*>? = null
    private var selectedNode: MusicTreeNode? = null
    private var soundLibrary = musicPack.getEditPackSoundLibrary()

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

        if (selectedPredicate?.type is ErrorPredicate) {
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
                    Component.translatableWithFallback("trueadaptivemusic.type", "Type").string,
                    { MusicPredicate.getDisplayName(it).string },
                    startingOption = selectedPredicateTypeName.takeIf { it.isNotBlank() },
                    tooltipText = Component.translatableWithFallback(
                        "trueadaptivemusic.predicate_type.description",
                        "Select under what circumstances the music should play"
                    )
                )
            },
            "predicateTypeChoice"
        )

        if (selectedPredicateTypeName in Constants.REQUIRES_SERVER_SUPPORT_TRIGGERS) {
            addWidgetFromRender(
                {
                    val widget = ClickableTextWidget(Constants.REQUIRES_SERVER_SUPPORT_TEXT).withItalic()
                    widget.setTooltip(Tooltip.create(Constants.REQUIRES_SERVER_SUPPORT_TOOLTIP))
                    widget
                },
                "serverOnlyWarning"
            )
        }

        requiredPredicateArgs.forEach { arg ->
            addWidgetFromRender(
                {
                    TAMAPI.makeInputWidget(
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

        if (selectedPredicate?.type !is RootPredicate) {
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
                                            "trueadaptivemusic.delete", "Delete").string
                                    )
                                    widget.color = CommonColors.WHITE
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
                Tooltip.create(
                    Component.translatableWithFallback(
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

    fun setEditExistingPredicate(node: MusicTreeNode, predicate: MusicPredicate<*>) {
        clearWidgetsFromRender()
        setSelectedPredicateTypeName(predicate.type.typeName)
        selectedPredicate = predicate
        selectedNode = node
        resetScrolling()
    }

    fun setCreateNewPredicate(parent: MusicTreeNode) {
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
        requiredPredicateArgs = TAMAPI.getPredicateTypeArguments(typeName).map { WidgetArg.of(it) }
        predicateArgs = selectedPredicate?.let {
            if (it.type.typeName == selectedPredicateTypeName)
                MusicTrigger.getTriggerArgs(it.arguments).map { arg -> arg.value }.toMutableList()
            else
                null
        } ?: requiredPredicateArgs.map { null }.toMutableList()

        clearWidgetsFromRender { childWidget -> childWidget.id in arrayOf("predicateTypeChoice", "musicChoice") }
    }

    private fun renderErrorMode() {
        val result = addWidgetFromRender(
            {
                ClickableTextWidget(
                    Component.translatableWithFallback("trueadaptivemusic.delete", "Delete").string,
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
            Tooltip.create(
                Component.translatableWithFallback(
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
        val predicateType = TAMAPI.getPredicateType(selectedPredicateTypeName)
            ?: throw MusicTriggerException("Unknown predicate type '$selectedPredicateTypeName'")
        val replacement =
            TAMClient.musicPredicateFactory.fromArgs(
                predicateType,
                TAMAPI.makePredicateArguments(
                    predicateType, predicateArgs.filterNotNull())
            )
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

    private fun makeNewPredicate(): MusicTreeNode? {
        return selectedNode?.newPredicate(selectedPredicateTypeName, predicateArgs)
    }
}
