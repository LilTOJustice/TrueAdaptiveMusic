package liltojustice.trueadaptivemusic.client.gui.widget

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.RenderState
import liltojustice.trueadaptivemusic.client.gui.screen.ExtensionsSuggestionScreen
import liltojustice.trueadaptivemusic.client.gui.screen.PackBrowserScreen
import liltojustice.trueadaptivemusic.client.gui.widget.utility.ClickableTextDisplayWidget
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import liltojustice.trueadaptivemusic.client.music.pack.MusicPackValidation
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.components.ObjectSelectionList
import net.minecraft.client.gui.components.LoadingDotsWidget
import com.mojang.blaze3d.platform.NativeImage
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import net.minecraft.resources.ResourceLocation
import net.minecraft.Util
import net.minecraft.util.Mth
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
    private val loadedPackImages = mutableSetOf<ResourceLocation>()

    init {
        init()
    }

    override fun getRowLeft(): Int {
        return x + 3
    }

    override fun getRowRight(): Int {
        return scrollbarPosition - 3
    }

    override fun getScrollbarPosition(): Int {
        return right - 8
    }

    override fun renderSelection(
        context: GuiGraphics,
        y: Int,
        entryWidth: Int,
        entryHeight: Int,
        borderColor: Int,
        fillColor: Int
    ) {
        val i = rowLeft
        val j = rowRight
        context.fill(i, y - 2, j, y + entryHeight + 2, borderColor)
        context.fill(i + 1, y - 1, j - 1, y + entryHeight + 1, fillColor)
    }

    override fun getEntryAtPosition(x: Double, y: Double): Entry? {
        val j = rowLeft
        val k = rowRight
        val m = Mth.floor(y - this.y.toDouble()) - this.headerHeight + this.scrollAmount.toInt() - 4
        val n = m / this.itemHeight
        return this.children().takeIf {
            x >= j.toDouble() && x <= k.toDouble() && m >= 0 && n < this.itemCount
        }?.get(n)
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        context.setColor(0f, 0f, 0f, 0.5f)
        context.fill(x, y, x + this.width, y + this.height, CommonColors.BLACK)
        context.setColor(1f, 1f, 1f, 1f)
        super.renderWidget(context, mouseX, mouseY, deltaTicks)
        if (renderState == RenderState.Loading) {
            loadingWidget.setPosition(
                x + (width - loadingWidget.width) / 2, y + (height - loadingWidget.height) / 2)
            loadingWidget.render(context, mouseX, mouseY, deltaTicks)

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
                PackBrowserEntry {
                    minecraft.setScreen(
                        if (TAMClient.extensions != null)
                            PackBrowserScreen(screen)
                        else
                            ExtensionsSuggestionScreen(screen)
                    )
                }
            )
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
        private val issuesButton =
            if (musicPack?.validationMessages?.isEmpty() != false) {
                null
            }
            else {
                val result = ClickableTextDisplayWidget(ISSUES_TEXT.string)
                result.setTooltip(Tooltip.create(getValidationText(musicPack.validationMessages)))

                result
            }

        fun getWidth(): Int {
            return scrollbarPosition - rowLeft - 3
        }

        override fun render(
            context: GuiGraphics,
            index: Int,
            y: Int,
            x: Int,
            entryWidth: Int,
            entryHeight: Int,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickDelta: Float
        ) {
            musicPack ?: return
            val entryWidth = getWidth()
            val packName = musicPack.packPath.nameWithoutExtension
            val extension = musicPack.packPath.extension
            renderPackImage(context,  x, y, entryHeight)
            renderScrollingString(
                context,
                minecraft.font,
                prettyPackNameText(extension != "zip", packName),
                x + 5 + entryHeight,
                x + 5 + entryHeight,
                y + 3,
                rowRight - 3,
                y + minecraft.font.lineHeight + 3,
                CommonColors.WHITE
            )
            issuesButton?.let {
                issuesButton.x = x + entryWidth - issuesButton.width - 5
                issuesButton.y = y + entryHeight - issuesButton.height - 5
                issuesButton.render(context, mouseX, mouseY, tickDelta)
            }

            renderScrollingString(
                context,
                minecraft.font,
                Component.literal(musicPack.options.description).withColor(CommonColors.GRAY),
                x + 5 + entryHeight,
                x + 5 + entryHeight,
                y + 17,
                (issuesButton?.x ?: rowRight) - 3,
                y + entryHeight,
                CommonColors.GRAY
            )
        }

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
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

        private fun renderPackImage(context: GuiGraphics, entryX: Int, entryY: Int, imageSize: Int) {
            musicPack ?: return
            val identifier = ResourceLocation.fromNamespaceAndPath(
                "trueadaptivemusic",
                "icon/" +
                        Util.sanitizeName(musicPack.packName, ResourceLocation::validPathChar)
            )

            if (identifier !in loadedPackImages) {
                musicPack.getIconStream()
                    ?.use {
                        minecraft.textureManager.register(
                            identifier,
                            DynamicTexture(NativeImage.read(it))
                        )
                        loadedPackImages.add(identifier)
                    }
            }

            context.blit(
                identifier,
                entryX + 2,
                entryY,
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
        override fun render(
            context: GuiGraphics,
            index: Int,
            y: Int,
            x: Int,
            entryWidth: Int,
            entryHeight: Int,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickDelta: Float
        ) {
            context.drawString(
                minecraft.font,
                VANILLA_TEXT,
                x + 3,
                y + 6,
                CommonColors.WHITE,
                false
            )
            context.drawString(
                minecraft.font,
                DISABLE_TAM_TEXT,
                x + 3, y + 14 + 3,
                CommonColors.GRAY,
                false
            )
        }
    }

    inner class PackBrowserEntry(private val onClick: () -> Unit): Entry(null) {
        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            onClick()

            return true
        }

        override fun render(
            context: GuiGraphics,
            index: Int,
            y: Int,
            x: Int,
            entryWidth: Int,
            entryHeight: Int,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickDelta: Float
        ) {
            context.drawString(
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