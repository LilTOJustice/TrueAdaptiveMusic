package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.MusicPack
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicate
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicateTree
import liltojustice.trueadaptivemusic.client.predicate.RootPredicate
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import kotlin.reflect.KParameter
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
        clearWidgetsFromRender()
        selectedNode = node
        newPredicateParent = null
    }

    fun setCreateNewNode(parent: MusicPredicateTree.Node) {
        clearWidgetsFromRender()
        selectedPredicateTypeName = ""
        newPredicateParent = parent
        selectedNode = null
    }

    private fun setSelectedPredicateTypeName(typeName: String) {
        selectedPredicateTypeName = typeName
        requiredArgs = MusicPredicate.getRequiredArgsFromTypeName(typeName)
        args = requiredArgs.map { null }.toMutableList()
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
        else {
            selectedPredicateTypeName = RootPredicate.getTypeName()
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
        addWidgetFromRender(
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
                                    selectedPredicateTypeName, args.filterNotNull().toTypedArray())
                            selectedNode!!.playableSounds = musicSelector.selected.mapNotNull { path -> assets[path] }
                        }
                        else {
                            newPredicateParent?.newChild(
                                selectedPredicateTypeName,
                                args = args.filterNotNull().toTypedArray(),
                                musicSelector.selected.mapNotNull { path -> assets[path] })
                        }
                        musicPack.initRules()
                        onChangesSaved()
                    })
            },
            "Create"
        )
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    private fun widgetMaker(arg: KParameter): ClickableWidget {
        return when(arg.type) {
            typeOf<Identifier>() -> {
                DropdownWidget(
                    Registries.REGISTRIES.flatMap { registry -> registry.ids.map { id -> id.path } },
                    { id -> args[arg.index] = Identifier(id) },
                    arg.name ?: "Identifier")
            }
            typeOf<String>() -> {
                val widget = TextFieldWidget(textRenderer, 0, 0, 0, 0, Text.literal(arg.name))
                widget.setChangedListener { value -> args[arg.index] = value }
                widget
            }
            else ->
                throw Exception("Couldn't create widget for expected type ${arg.type}.")
        }
    }

}

