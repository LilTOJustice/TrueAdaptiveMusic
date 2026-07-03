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
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gui.Click
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.Screen.MENU_BACKGROUND_TEXTURE
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget
import net.minecraft.client.gui.widget.LoadingWidget
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension

class PackListWidget(
    private val screen: Screen,
    client: MinecraftClient,
    width: Int,
    height: Int,
    top: Int,
    itemHeight: Int,
    private val onSelectPack: (selectedPack: MusicPack?) -> Unit = {}
): AlwaysSelectedEntryListWidget<PackListWidget.Entry>(
    client, width, height, top, itemHeight) {
    private var renderState = RenderState.Loading
    private val backgroundScope = CoroutineScope(EmptyCoroutineContext)
    private val loadingWidget = LoadingWidget(this.client.textRenderer, LOADING_TEXT)
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

    override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        context?.drawTexture(
            RenderPipelines.GUI_TEXTURED,
            MENU_BACKGROUND_TEXTURE,
            x,
            y,
            0F,
            0F,
            width,
            height,
            32,
            32
        )
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
                    minecraft.gui.setScreen(
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
        private val VANILLA_TEXT = Text.translatableWithFallback("trueadaptivemusic.vanilla", "Vanilla")
        private val DISABLE_TAM_TEXT = Text.translatableWithFallback(
            "trueadaptivemusic.disable_tam", "Disable True Adaptive Music")
        private val ISSUES_TEXT = Text.translatableWithFallback(
            "trueadaptivemusic.issues_found", "Issues Found")
        private val PACK_BROWSER_TEXT = Text.translatableWithFallback(
            "trueadaptivemusic.open_pack_browser", "Get More Packs")
            .setStyle(Style.EMPTY.withBold(true).withItalic(true).withUnderline(true))
        private val LOADING_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.loading_packs", "Loading Packs")
        private val EDIT_OF_PREFIX: Text = Text
            .translatableWithFallback("trueadaptivemusic.edit_of", "Edit of ")
            .getWithStyle(Style.EMPTY.withItalic(true))
            .first()
        private fun getValidationText(validation: List<MusicPackValidation.ValidationMessage>): Text {
            val warnings = validation.filter { it.type == MusicPackValidation.ValidationMessage.Type.Warning }
            val errors = validation.filter { it.type == MusicPackValidation.ValidationMessage.Type.Error }
            val result = Text.empty()
            if (warnings.isNotEmpty()) {
                result.append(
                    Text.translatableWithFallback(
                        "trueadaptivemusic.warning_count",
                        "${warnings.size} warning(s)",
                        warnings.size.toString()
                    )
                )
            }

            if (warnings.isNotEmpty() && errors.isNotEmpty()) {
                result.append(" ${Text.translatableWithFallback("trueadaptivemusic.and", "and")} ")
            }

            if (errors.isNotEmpty()) {
                result.append(
                    Text.translatableWithFallback(
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

        private fun prettyPackNameText(isEdit: Boolean, packName: String): Text {
            return if (isEdit)
                EDIT_OF_PREFIX.copy().append(Text.literal(packName))
            else
                Text.literal(packName)
        }
    }

    open inner class Entry(private val musicPack: MusicPack?): AlwaysSelectedEntryListWidget.Entry<Entry>() {
        private val imageSize
            get() = height
        private val issuesButton =
            if (musicPack?.validationMessages?.isEmpty() != false) {
                null
            }
            else {
                val result = ClickableTextDisplayWidget(ISSUES_TEXT.string)
                result.setTooltip(Tooltip.of(getValidationText(musicPack.validationMessages)))

                result
            }

        override fun getWidth(): Int {
            return scrollbarX - rowLeft - 3
        }

        override fun render(
            context: DrawContext,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickDelta: Float
        ) {
            musicPack ?: return
            renderPackImage(context)
            val packName = musicPack.packPath.nameWithoutExtension
            val extension = musicPack.packPath.extension
            drawScrollableText(
                context,
                client.textRenderer,
                prettyPackNameText(extension != "zip", packName),
                x + 5 + imageSize,
                x + 5 + imageSize,
                y + 3,
                rowRight - 3,
                y + client.textRenderer.fontHeight + 3,
                Colors.WHITE
            )
            issuesButton?.let {
                issuesButton.x = x + width - issuesButton.width - 5
                issuesButton.y = y + height - issuesButton.height - 5
                issuesButton.render(context, mouseX, mouseY, tickDelta)
            }

            drawScrollableText(
                context,
                client.textRenderer,
                Text.literal(musicPack.options.description).withColor(Colors.GRAY),
                x + 5 + imageSize,
                x + 5 + imageSize,
                y + 17,
                (issuesButton?.x ?: rowRight) - 3,
                y + height,
                Colors.GRAY
            )
        }

        override fun mouseClicked(click: Click, doubled: Boolean): Boolean {
            if (selectedOrNull == this) {
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

        override fun getNarration(): Text {
            return Text.empty()
        }

        private fun renderPackImage(context: DrawContext) {
            musicPack ?: return
            val identifier = Identifier.of(
                "trueadaptivemusic",
                "icon/" +
                        Util.replaceInvalidChars(musicPack.packName, Identifier::isPathCharacterValid)
            )

            if (identifier !in loadedPackImages) {
                musicPack.getIconStream()
                    .use {
                        client.textureManager.registerTexture(
                            identifier,
                            NativeImageBackedTexture(
                                identifier::toString, NativeImage.read(it))
                        )
                        loadedPackImages.add(identifier)
                    }
            }

            context.drawTexture(
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
        override fun render(context: DrawContext, mouseX: Int, mouseY: Int, hovered: Boolean, tickDelta: Float) {
            context.drawText(
                client.textRenderer,
                VANILLA_TEXT,
                x + 3,
                y + 6,
                Colors.WHITE,
                false
            )
            context.drawText(
                client.textRenderer,
                DISABLE_TAM_TEXT,
                x + 3, y + 14 + 3,
                Colors.GRAY,
                false
            )
        }
    }

    inner class PackBrowserEntry(private val onClick: () -> Unit): Entry(null) {
        override fun mouseClicked(click: Click, doubled: Boolean): Boolean {
            onClick()

            return true
        }

        override fun render(context: DrawContext, mouseX: Int, mouseY: Int, hovered: Boolean, tickDelta: Float) {
            context.drawText(
                client.textRenderer,
                PACK_BROWSER_TEXT,
                x + 3,
                y + 6,
                Colors.WHITE,
                true
            )
        }
    }
}