package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.MusicPack
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicate
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicateTree
import liltojustice.trueadaptivemusic.client.predicate.RootPredicate
import liltojustice.trueadaptivemusic.client.identifier.TypedIdentifier
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.Identifier
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

class PredicateViewWidget(
    width: Int,
    height: Int,
    private val musicPack: MusicPack,
    private val onChangesSaved: () -> Unit,
    x: Int = 0,
    y: Int = 0)
    : ContainerWidget(width, height, "Predicate View", true, false, true, x, y) {
    private val predicateTypeNameOptions = MusicPredicate.getTypeNames()
        .filter { typeName -> typeName != RootPredicate.getTypeName() }
    private var requiredArgs = listOf<KParameter>()
    private var args = mutableListOf<Any?>()

    private var selectedPredicateTypeName: String = predicateTypeNameOptions.firstOrNull() ?: ""
    private var selectedNode: MusicPredicateTree.Node? = null
    private var newPredicateParent: MusicPredicateTree.Node? = null
    private var movingNode: MusicPredicateTree.Node? = null

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        if (!visible) {
            return
        }

        if (newPredicateParent != null || selectedNode != null) {
            renderEditMode(context)
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
        movingNode?.let {
            if (node == it) {
                return@let
            }

            node.adoptChildFront(it)
            save()
        }

        clearWidgetsFromRender()
        setSelectedPredicateTypeName(node.predicate.getTypeName())
        selectedNode = node
        newPredicateParent = null
        movingNode = null
    }

    fun setCreateNewNode(parent: MusicPredicateTree.Node) {
        movingNode?.let {
            if (parent == it) {
                return@let
            }

            if (parent.adoptChild(it)) {
                save()
            }
        }

        clearWidgetsFromRender()
        selectedPredicateTypeName = ""
        selectedNode = null
        newPredicateParent = parent
        movingNode = null
        requiredArgs = listOf()
        args = mutableListOf()
    }

    private fun setSelectedPredicateTypeName(typeName: String) {
        selectedPredicateTypeName = typeName
        requiredArgs = MusicPredicate.getRequiredArgsFromTypeName(typeName)
        args = selectedNode?.let {
            if (it.predicate.getTypeName() == selectedPredicateTypeName)
                it.predicate.getIDs().toMutableList()
            else null
        } ?: requiredArgs.map { null }.toMutableList()

        clearWidgetsFromRender { childWidget -> childWidget.id in arrayOf("predicateTypeChoice", "musicChoice") }
    }

    private fun renderEditMode(context: DrawContext?) {
        drawCenteredText(
            context,
            if (selectedNode != null) "Edit Prediate" else "New Predicate",
            0,
            width / 2)
        if (selectedNode?.predicate?.getTypeName() != RootPredicate.getTypeName()) {
            addWidgetFromRender(
                {
                    DropdownWidget(
                        predicateTypeNameOptions,
                        { typeName ->  setSelectedPredicateTypeName(typeName) },
                        "Type",
                        startingOption = selectedPredicateTypeName)
                },
                "predicateTypeChoice",
                row = 1)
        }

        val musicSelector = addWidgetFromRender(
            {
                MultiSelectDropdownWidget(
                    listOf(),
                    "Music Choice",
                    { musicPack.getEditPackAssets().map { (assetName, _) -> assetName }.toMutableList() },
                    "Select a track",
                    selectedNode?.playableSounds?.map { sound -> sound.getSoundName() } ?: listOf())
            },
            "musicChoice"
        ) as MultiSelectDropdownWidget
        requiredArgs.forEach { arg ->
            addWidgetFromRender(
                { widgetMaker(arg) },
                "arg: ${arg.name ?: arg.index}"
            )
        }

        val saveWidget = addWidgetFromRender(
            {
                ClickableTextWidget(
                    "Save",
                    onClick = {
                        val assets = musicPack.getEditPackAssets()
                        if (selectedNode != null) {
                            selectedNode!!.predicate =
                                if (selectedNode!!.predicate.getTypeName() == RootPredicate.getTypeName())
                                    selectedNode!!.predicate
                                else MusicPredicate.initializeFromArgs(
                                    selectedPredicateTypeName, *args.filterNotNull().toTypedArray())
                            selectedNode!!.playableSounds = musicSelector.selected.mapNotNull { path -> assets[path] }
                        }
                        else {
                            newPredicateParent?.newChild(
                                selectedPredicateTypeName,
                                args = args.filterNotNull().toTypedArray(),
                                musicSelector.selected.mapNotNull { path -> assets[path] })
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

            if (movingNode == null) {
                addWidgetFromRender(
                    {
                        ClickableTextWidget(
                            "Move",
                            onClick = {
                                movingNode = selectedNode
                                clearWidgetsFromRender { childWidget -> childWidget.id != "Move" }
                            }
                        )
                    },
                    "Move"
                )
            }
        }

        if (movingNode != null) {
            addWidgetFromRender(
                {
                    ClickableTextWidget(
                        "Cancel Move",
                        onClick = {
                            movingNode = null
                            clearWidgetsFromRender { childWidget -> childWidget.id != "Cancel Move" }
                        },
                        isSelected = { movingNode != null }
                    )
                },
                "Cancel Move"
            )
        }

        saveWidget.active = args.filterNotNull().size == requiredArgs.size
        saveWidget.color = if (saveWidget.active) Colors.WHITE else Colors.RED
        saveWidget.tooltip = if (saveWidget.active)
            null
        else if (saveWidget.tooltip == null)
            Tooltip.of(Text.literal("Can't access required dynamic registry. Try again while a world is loaded."))
        else
            saveWidget.tooltip
    }

    private fun widgetMaker(arg: KParameter): ClickableWidget {
        return if (arg.type == typeOf<Identifier>()) {
            DropdownWidget(
                Registries.REGISTRIES.flatMap { registry -> registry.ids.map { id -> id.path } },
                { id -> args[arg.index] = Identifier(id) },
                (arg.name ?: "Unknown") + ": Identifier",
                startingOption = args[arg.index] as? String ?: "")
        }
        else if (arg.type.isSubtypeOf(typeOf<TypedIdentifier>())) {
            val options = TypedIdentifier.getRegistryIdsFromType(arg.type).map { id -> id.toString() }.sorted()
            return if (options.isEmpty())
                EmptyClickableWidget()
            else
                DropdownWidget(
                    options,
                    { id -> args[arg.index] = TypedIdentifier.initializeFromIdString(arg.type, id) },
                    (arg.name ?: "Unknown") + ": ${arg.type.toString().split('.').last()}",
                    startingOption = args[arg.index] as? String ?: "")
        }
        else {
            throw Exception("Couldn't create widget for expected type ${arg.type}.")
        }
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
}

