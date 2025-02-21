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
    : ContainerWidget(width, height, "Predicate View", true, false, x, y) {
    private var selectedPredicate: MusicPredicate? = null
    private var newPredicateParent: MusicPredicateTree.Node? = null
    private var selectedNewPredicateTypeName: String? = null
    private val predicateTypeNameOptions = MusicPredicate.getTypeNames()
        .filter { typeName -> typeName != RootPredicate.getTypeName() }
    private var newPredicateMode = false
    private var requiredArgs = listOf<KParameter>()
    private var args = mutableListOf<Any?>()
    private var musicList = mutableListOf<String>()

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        if (!visible) {
            return
        }

        if (newPredicateMode) {
            renderNewPredicateMode(context)
        }
        else if (selectedPredicate != null) {
            renderEditPredicateMode(context)
        }
        else {
            drawCenteredText(
                context,
                "Select or add a predicate",
                0,
                width / 2)
        }
    }

    fun setEditPredicate(predicate: MusicPredicate) {
        clearWidgetsFromRender()
        selectedPredicate = predicate
        newPredicateMode = false
    }

    fun unsetEditPredicate() {
        clearWidgetsFromRender()
        selectedPredicate = null
        newPredicateMode = false
    }

    fun setCreateNewPredicate(parent: MusicPredicateTree.Node) {
        clearWidgetsFromRender()
        newPredicateParent = parent
        newPredicateMode = true
    }

    fun setNewPredicateTypeName(typeName: String) {
        selectedNewPredicateTypeName = typeName
        requiredArgs = MusicPredicate.getRequiredArgsFromTypeName(typeName)
        args = requiredArgs.map { null }.toMutableList()
        clearWidgetsFromRender { childWidget -> childWidget.id in arrayOf("predicateTypeChoice", "musicChoice") }
    }

    private fun renderNewPredicateMode(context: DrawContext?) {
        drawCenteredText(
            context,
            "New Predicate",
            0,
            width / 2)
        addWidgetFromRender(
            {
                DropdownWidget(
                    predicateTypeNameOptions.toMutableList(),
                    { typeName ->  setNewPredicateTypeName(typeName) },
                    "Type")
            },
            "predicateTypeChoice",
            row = 1)
        addWidgetFromRender(
            {
                DropdownWidget(
                    listOf(),
                    { sound -> musicList.add(sound) },
                    "Music Choice",
                    { musicPack.getEditPackAssets().map { (assetName, _) -> assetName }.toMutableList() },
                    "Select a track")
            },
            "musicChoice"
        )
        requiredArgs.forEach { arg ->
            addWidgetFromRender(
                { widgetMaker(arg) },
                "arg: ${arg.name ?: arg.index}"
            )
        }
        addWidgetFromRender(
            {
                ClickableTextWidget("Create",
                    onClick = {
                        val assets = musicPack.getEditPackAssets()
                        newPredicateParent?.newChild(
                            selectedNewPredicateTypeName!!,
                            args = args.filterNotNull().toTypedArray(),
                            musicList.mapNotNull { path -> assets[path] })
                        musicPack.initRules()
                        onChangesSaved()
                    })
            },
            "Create"
        )
    }

    private fun renderEditPredicateMode(context: DrawContext?) {
        val selectedPredicate = selectedPredicate!!
        drawCenteredText(
            context,
            selectedPredicate.getTypeName(),
            0,
            width / 2)
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
                throw Exception("Couldn't create widget for expected type $type.")
        }
    }

}

