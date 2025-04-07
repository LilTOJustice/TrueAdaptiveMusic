package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.LogLevel
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.Callbacks
import liltojustice.trueadaptivemusic.client.MusicPack
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicateTree
import liltojustice.trueadaptivemusic.client.predicate.types.RootPredicate
import liltojustice.trueadaptivemusic.client.identifier.TypedIdentifier
import liltojustice.trueadaptivemusic.client.predicate.types.MusicPredicate
import liltojustice.trueadaptivemusic.client.sound.PlayableSound
import liltojustice.trueadaptivemusic.client.sound.PlayableSoundEvent
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.registry.Registries
import net.minecraft.sound.SoundEvent
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.Identifier
import net.minecraft.util.InvalidIdentifierException
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor
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
    private var requiredPredicateArgs = listOf<KParameter>()
    private var predicateArgs = mutableListOf<Any?>()
    private val requiredNodeArgs = MusicPredicateTree.Node.Parameters::class.primaryConstructor?.parameters ?: listOf()
    private var nodeArgs: MutableList<Any?> = requiredNodeArgs.map { null }.toMutableList()
    private var selectedPredicateTypeName: String = predicateTypeNameOptions.firstOrNull() ?: ""
    private var selectedNode: MusicPredicateTree.Node? = null
    private var newPredicateParent: MusicPredicateTree.Node? = null
    private var movingNode: MusicPredicateTree.Node? = null
    private var selectedMusicPaths = mutableListOf<String>()
    private var assets = musicPack.getEditPackAssets()

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        if (!visible) {
            return
        }

        if (newPredicateParent != null || selectedNode != null) {
            renderEditMode()
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
        selectedMusicPaths = selectedNode!!.playableSounds.map { sound -> sound.getSoundName() }.toMutableList()
        newPredicateParent = null
        movingNode = null
        nodeArgs = node.parameters.constructorParams().toMutableList()
        resetScrolling()
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
        selectedMusicPaths = mutableListOf()
        newPredicateParent = parent
        movingNode = null
        requiredPredicateArgs = listOf()
        predicateArgs = mutableListOf()
        nodeArgs.replaceAll { null }
        resetScrolling()
    }

    private fun setSelectedPredicateTypeName(typeName: String) {
        selectedPredicateTypeName = typeName
        requiredPredicateArgs = MusicPredicate.getRequiredArgsFromTypeName(typeName)
        predicateArgs = selectedNode?.let {
            if (it.predicate.getTypeName() == selectedPredicateTypeName)
                it.predicate.getTriggerParams().map { param -> param.value }.toMutableList()
            else
                null
        } ?: requiredPredicateArgs.map { null }.toMutableList()

        clearWidgetsFromRender { childWidget -> childWidget.id in arrayOf("predicateTypeChoice", "musicChoice") }
    }

    private fun renderEditMode() {
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

        addWidgetFromRender(
            {
                MultiSelectDropdownWidget(
                    listOf(),
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
                    onHover = { option -> Callbacks.playSoundNow(option?.let { toPlayableSound(assets, it) }) })
            },
            "musicChoice"
        ) as MultiSelectDropdownWidget

        requiredPredicateArgs.forEach { arg ->
            addWidgetFromRender(
                { widgetMaker(screen!!, predicateArgs, arg) },
                "predicateArg: ${arg.name ?: arg.index}"
            )
        }

        requiredNodeArgs.forEach { arg ->
            addWidgetFromRender(
                { widgetMaker(screen!!, nodeArgs, arg) },
                "nodeArg: ${arg.name ?: arg.index}"
            )
        }

        val saveWidget = addWidgetFromRender(
            {
                ClickableTextWidget(
                    "Save",
                    onClick = {
                        assets = musicPack.getEditPackAssets()
                        if (selectedNode != null) {
                            selectedNode!!.predicate =
                                if (selectedNode!!.predicate.getTypeName() == RootPredicate.getTypeName())
                                    selectedNode!!.predicate
                                else MusicPredicate.initializeFromArgs(
                                    selectedPredicateTypeName, *predicateArgs.filterNotNull().toTypedArray())
                            selectedNode!!.playableSounds = selectedMusicPaths
                                .mapNotNull { path -> toPlayableSound(assets, path) }
                            selectedNode!!.parameters =
                                MusicPredicateTree.Node.Parameters.initializeFromArgs(
                                    *nodeArgs.filterNotNull().toTypedArray())
                        }
                        else {
                            newPredicateParent?.newChild(
                                selectedPredicateTypeName,
                                nodeArgs.filterNotNull(),
                                predicateArgs.filterNotNull(),
                                selectedMusicPaths.mapNotNull { path -> toPlayableSound(assets, path) })
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

        saveWidget.active = predicateArgs.filterNotNull().size == requiredPredicateArgs.size
        saveWidget.color = if (saveWidget.active) Colors.WHITE else Colors.RED
        saveWidget.tooltip =
            if (saveWidget.active)
                null
            else if (requiredPredicateArgs.any { arg ->
                isTypedIdentifierList(arg.type)
                        && TypedIdentifier.getRegistryIdsFromType(arg.type.arguments.firstOrNull()!!.type!!)
                            .isEmpty() })
                DYNAMIC_REGISTRY_TOOLTIP
            else
                MISSING_ARGS_TOOLTIP
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
        private fun toPlayableSound(assets: Map<String, PlayableSound>, id: String): PlayableSound? {
            return assets[id] ?: try {
                PlayableSoundEvent(SoundEvent.of(Identifier(id)))
            }
            catch (e: InvalidIdentifierException) {
                null
            }
        }

        private fun widgetMaker(screen: Screen, outArgs: MutableList<Any?>, arg: KParameter): ClickableWidget {
            val prompt = (arg.name ?: "Unknown") +
                    ": ${arg.type.toString().split('.').last().replace(">", "")}"
            return if (arg.type == typeOf<Int>()) {
                TextInputWidget(
                    screen,
                    prompt,
                    30,
                    { widget, text ->
                        if (text == "0-") {
                            widget.text = "-0"
                            return@TextInputWidget
                        }

                        val value = text.toIntOrNull()
                        if (text != "-0" && value == null) {
                            widget.text = "0"
                            return@TextInputWidget
                        }

                        if (text != "-0" && text != value.toString()) {
                            widget.text = value.toString()
                            return@TextInputWidget
                        }

                        outArgs[arg.index] = value
                    },
                    outArgs[arg.index]?.toString() ?: ""
                )
            }
            else if (arg.type == typeOf<UInt>()) {
                TextInputWidget(
                    screen,
                    prompt,
                    30,
                    { widget, text ->
                        val value = text.toUIntOrNull()
                        if (value == null) {
                            widget.text = "0"
                            return@TextInputWidget
                        }

                        if (text != value.toString()) {
                            widget.text = value.toString()
                            return@TextInputWidget
                        }

                        outArgs[arg.index] = value
                    },
                    outArgs[arg.index]?.toString() ?: ""
                )
            }
            else if (arg.type == typeOf<Boolean>()) {
                CheckboxWidget(
                    10,
                    prompt,
                    { checked -> outArgs[arg.index] = checked },
                    checked = outArgs[arg.index] as? Boolean ?: false)
            }
            else if (arg.type.isSubtypeOf(typeOf<Enum<*>>())) {
                val enumClass = (arg.type.classifier as KClass<*>).java
                val options = enumClass.enumConstants.map { enum -> enum.toString() }

                if (options.isEmpty())
                    EmptyClickableWidget()
                else
                    DropdownWidget(
                        options,
                        { enumOption -> outArgs[arg.index] = enumClass.enumConstants.first { enum -> enum.toString() == enumOption} },
                        prompt,
                        startingOption = (outArgs[arg.index] as? Enum<*>)?.name ?: "")
            }
            else if (isEnumList(arg.type)) {
                val type = arg.type.arguments.firstOrNull()?.type
                    ?: throw Exception("Somehow Enum didn't have any type args. The world is chaos.")
                val enumClass = (type.classifier as KClass<*>).java
                val options = enumClass.enumConstants.map { enum -> enum.toString() }
                MultiSelectDropdownWidget(
                    options,
                    { selected -> outArgs[arg.index] = selected
                        .map { enumOption -> enumClass.enumConstants.first { enum -> enum.toString() == enumOption } }
                        .ifEmpty { null } },
                    "${prompt}s",
                    notSelectedPlaceholder = "Select a value",
                    alreadySelected = (outArgs[arg.index] as? List<*>)?.map { enum -> enum.toString() } ?: listOf())
            }
            else if (arg.type.isSubtypeOf(typeOf<TypedIdentifier>())) {
                val options = TypedIdentifier.getRegistryIdsFromType(arg.type).map { id -> id.toString() }.sorted()

                if (options.isEmpty())
                    EmptyClickableWidget()
                else
                    DropdownWidget(
                        options,
                        { id -> outArgs[arg.index] = TypedIdentifier.initializeFromIdString(arg.type, id) },
                        prompt,
                        startingOption = (outArgs[arg.index] as? TypedIdentifier)?.toString() ?: "")
            }
            else if (isTypedIdentifierList(arg.type)) {
                val type = arg.type.arguments.firstOrNull()?.type
                    ?: throw Exception("Somehow List didn't have any type args. The world is chaos.")
                val options = TypedIdentifier.getRegistryIdsFromType(type).map { id -> id.toString() }.sorted()

                if (options.isEmpty())
                    EmptyClickableWidget()
                else
                    MultiSelectDropdownWidget(
                        options,
                        { selected -> outArgs[arg.index] = selected
                            .map { id -> TypedIdentifier.initializeFromIdString(type, id) }
                            .ifEmpty { null } },
                        "${prompt}s",
                        notSelectedPlaceholder = "Select an Identifier",
                        alreadySelected = (outArgs[arg.index] as? List<*>)?.map { id -> id.toString() } ?: listOf())
            }
            else {
                Logger.log("Couldn't create widget for expected type ${arg.type}.", LogLevel.WARNING)
                EmptyClickableWidget()
            }
        }

        private fun isEnumList(type: KType): Boolean {
            return type.isSubtypeOf(typeOf<List<*>>())
                    && type.arguments.any { typeArg -> typeArg.type?.isSubtypeOf(typeOf<Enum<*>>()) == true }
        }

        private fun isTypedIdentifierList(type: KType): Boolean {
            return type.isSubtypeOf(typeOf<List<*>>())
                    && type.arguments.any { typeArg -> typeArg.type?.isSubtypeOf(typeOf<TypedIdentifier>()) == true }
        }

        private val DYNAMIC_REGISTRY_TOOLTIP =
            Tooltip.of(Text.literal("Can't access required dynamic registry. Try again while a world is loaded."))

        private val MISSING_ARGS_TOOLTIP =
            Tooltip.of(Text.literal("At least one required parameter for this type is missing."))
    }
}

