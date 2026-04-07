package liltojustice.trueadaptivemusic.client.gui.widget

import com.mojang.blaze3d.platform.NativeImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.RenderState
import liltojustice.trueadaptivemusic.client.gui.screen.PackBrowserScreen
import liltojustice.trueadaptivemusic.client.gui.widget.utility.ClickableTextDisplayWidget
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import liltojustice.trueadaptivemusic.client.music.pack.MusicPackValidation
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.LoadingDotsWidget
import net.minecraft.client.gui.components.ObjectSelectionList
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.resources.Identifier
import net.minecraft.util.CommonColors
import net.minecraft.util.Util
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension

class PackListWidget(
    private val screen: Screen,
    client: Minecraft,
    width: Int,
    height: Int,
    top: Int,
    itemHeight: Int,
    private val onSelectPack: (selectedPack: MusicPack?) -> Unit = {}
): ObjectSelectionList<PackListWidget.Entry>(
    client, width, height, top, itemHeight) {
    private var renderState = RenderState.Loading
    private val backgroundScope = CoroutineScope(EmptyCoroutineContext)
    private val loadingWidget = LoadingDotsWidget(this.minecraft.font, LOADING_TEXT)
    private val loadedPackImages = mutableSetOf<Identifier>()

    init {
        init()
    }

    override fun getRowLeft(): Int {
        return x + 3
    }

    override fun getRowRight(): Int {
        return right - 16
    }

    override fun extractWidgetRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        graphics.blit(
            RenderPipelines.GUI_TEXTURED,
            Screen.MENU_BACKGROUND,
            x,
            y,
            0F,
            0F,
            width,
            height,
            32,
            32
        )
        super.extractWidgetRenderState(graphics, mouseX, mouseY, a)
        if (renderState == RenderState.Loading) {
            loadingWidget.setPosition(
                x + (width - loadingWidget.width) / 2, y + (height - loadingWidget.height) / 2)
            loadingWidget.extractRenderState(graphics, mouseX, mouseY, a)

            return
        }
    }

    fun init() {
        backgroundScope.launch {
            renderState = RenderState.Loading
            clearEntries()
            val packs = MusicPack.loadAllPacks()
            val vanillaEntry = VanillaEntry()
            addEntry(vanillaEntry)
            setSelected(vanillaEntry)
                packs.forEach { musicPack ->
                    val newEntry = Entry(musicPack)
                    addEntry(newEntry)
                    if (musicPack.packName == TAMClient.musicPack?.packName) {
                        setSelected(newEntry)
                    }
                }
            addEntry(
                PackBrowserEntry { minecraft.setScreen(PackBrowserScreen(screen)) })
            renderState = RenderState.Success
        }
    }

    companion object {
        private val VANILLA_TEXT = Component.translatableWithFallback("trueadaptivemusic.vanilla", "Vanilla")
        private val DISABLE_TAM_TEXT = Component.translatableWithFallback(
        "trueadaptivemusic.disable_tam", "Disable True Adaptive Music")
        private val ISSUES_TEXT = Component.translatableWithFallback(
            "trueadaptivemusic.issues_found", "Issues Found")
        private val PACK_BROWSER_TEXT = Component.translatableWithFallback(
            "trueadaptivemusic.open_pack_browser", "Get More Packs")
            .setStyle(Style.EMPTY.withBold(true).withItalic(true).withUnderlined(true))
        private val LOADING_TEXT: MutableComponent = Component.translatableWithFallback(
            "trueadaptivemusic.loading_packs", "Loading Packs")
        private val EDIT_OF_PREFIX: Component = Component
            .translatableWithFallback("trueadaptivemusic.edit_of", "Edit of ")
            .toFlatList(Style.EMPTY.withItalic(true))
            .first()
        private fun getValidationText(validation: List<MusicPackValidation.ValidationMessage>): Component {
            val warnings = validation.filter { it.type == MusicPackValidation.ValidationMessage.Type.Warning }
            val errors = validation.filter { it.type == MusicPackValidation.ValidationMessage.Type.Error }
            val result = Component.empty()
            if (warnings.isNotEmpty()) {
                result.append(
                    Component.translatableWithFallback(
                        "trueadaptivemusic.warning_count",
                        "${warnings.size} warning(s)",
                        warnings.size.toString()
                    )
                )
            }

            if (warnings.isNotEmpty() && errors.isNotEmpty()) {
                result.append(" ${Component.translatableWithFallback("trueadaptivemusic.and", "and")} ")
            }

            if (errors.isNotEmpty()) {
                result.append(
                    Component.translatableWithFallback(
                        "trueadaptivemusic.error_count",
                        "${errors.size} error(s)",
                        errors.size.toString()
                    )
                )
            }

            if (warnings.isNotEmpty() || errors.isNotEmpty()) {
                result.append("\n\n")
            }

            result.append(validation.joinToString("\n\n") { message -> message.toString() })

            return result
        }

        private fun prettyPackNameText(isEdit: Boolean, packName: String): Component {
            return if (isEdit)
                EDIT_OF_PREFIX.copy().append(Component.literal(packName))
            else
                Component.literal(packName)
        }
    }

    open inner class Entry(private val musicPack: MusicPack?): ObjectSelectionList.Entry<Entry>() {
        private val imageSize
            get() = height
        private val issuesButton =
            if (musicPack?.validationMessages?.isEmpty() != false) {
                null
            }
            else {
                val result = ClickableTextDisplayWidget(ISSUES_TEXT.string)
                result.setTooltip(Tooltip.create(getValidationText(musicPack.validationMessages)))

                result
            }

        override fun getWidth(): Int {
            return scrollBarX() - rowLeft - 3
        }

        override fun extractContent(
            graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, hovered: Boolean, a: Float) {
            musicPack ?: return
            renderPackImage(graphics)
            val packName = musicPack.packPath.nameWithoutExtension
            val extension = musicPack.packPath.extension
            graphics.textRenderer().acceptScrolling(
                prettyPackNameText(extension != "zip", packName),
                x + 5 + imageSize,
                x + 5 + imageSize,
                rowRight - 3,
                y + 3,
                y + minecraft.font.lineHeight + 3,
            )
            issuesButton?.let {
                it.x = x + width - it.width - 5
                it.y = y + height - it.height - 5
                it.extractRenderState(graphics, mouseX, mouseY, a)
            }

            graphics.textRenderer().acceptScrolling(
                Component.literal(musicPack.options.description).withColor(CommonColors.GRAY),
                x + 5 + imageSize,
                x + 5 + imageSize,
                (issuesButton?.x ?: rowRight) - 3,
                y + 17,
                y + height
            )
        }

        override fun mouseClicked(event: MouseButtonEvent, doubled: Boolean): Boolean {
            if (selected == this) {
                return true
            }

            if (this.musicPack?.isValid == false)
            {
                return false
            }

            setSelected(this)
            onSelectPack(musicPack)

            return true
        }

        override fun getNarration(): Component {
            return Component.empty()
        }

        private fun renderPackImage(graphics: GuiGraphicsExtractor) {
            musicPack ?: return
            val identifier = Identifier.fromNamespaceAndPath(
                "trueadaptivemusic",
                "icon/${Util.sanitizeName(musicPack.packName, Identifier::validPathChar)}"
            )

            if (identifier !in loadedPackImages) {
                musicPack.getIconStream()
                    ?.use {
                        minecraft.textureManager.register(
                            identifier,
                            DynamicTexture(
                                identifier::toString, NativeImage.read(it))
                        )
                        loadedPackImages.add(identifier)
                    }
            }

            graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                identifier,
                x + 2,
                y,
                0F,
                0F,
                imageSize,
                imageSize,
                imageSize,
                imageSize
            )
        }
    }

    inner class VanillaEntry: Entry(null) {
        override fun extractContent(
            graphics: GuiGraphicsExtractor,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            a: Float
        ) {
            graphics.text(
                minecraft.font,
                VANILLA_TEXT,
                x + 3,
                y + 6,
                CommonColors.WHITE,
                false
            )
            graphics.text(
                minecraft.font,
                DISABLE_TAM_TEXT,
                x + 3, y + 14 + 3,
                CommonColors.GRAY,
                false
            )
        }
    }

    inner class PackBrowserEntry(private val onClick: () -> Unit): Entry(null) {
        override fun mouseClicked(event: MouseButtonEvent, doubled: Boolean): Boolean {
            onClick()

            return true
        }

        override fun extractContent(
            graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, hovered: Boolean, a: Float) {
            graphics.text(
                minecraft.font,
                PACK_BROWSER_TEXT,
                x + 3,
                y + 6,
                CommonColors.WHITE,
                true
            )
        }
    }
}