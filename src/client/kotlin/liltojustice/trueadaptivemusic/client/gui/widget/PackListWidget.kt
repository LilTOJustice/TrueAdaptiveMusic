package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.widget.utility.ClickableTextDisplayWidget
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import liltojustice.trueadaptivemusic.client.music.pack.MusicPackValidation
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gui.Click
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen.MENU_BACKGROUND_TEXTURE
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget
import net.minecraft.text.Text
import net.minecraft.util.Colors

class PackListWidget(
    client: MinecraftClient,
    width: Int,
    height: Int,
    top: Int,
    itemHeight: Int,
    private val onSelectPack: (selectedPack: MusicPack?) -> Unit = {})
    : AlwaysSelectedEntryListWidget<PackListWidget.Entry>(client, width, height, top, itemHeight) {
    init {
        init()
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
    }

    fun init() {
        clearEntries()
        val vanillaEntry = Entry(this, client, null, onSelectPack)
        addEntry(vanillaEntry)
        setSelected(vanillaEntry)
        MusicPack.loadAllPacks()
            .forEach { musicPack ->
                val newEntry = Entry(this, client, musicPack, onSelectPack)
                addEntry(newEntry)
                if (musicPack.packName == TAMClient.musicPack?.packName) {
                    setSelected(newEntry)
                }
            }
    }

    companion object {
        private val issuesText = Text.translatableWithFallback(
            "trueadaptivemusic.issues_found", "Issues Found")
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
    }

    inner class Entry(
        private val packListWidget: PackListWidget,
        private val client: MinecraftClient,
        private val musicPack: MusicPack?,
        private val onSelectPack: (selectedPack: MusicPack?) -> Unit)
        : AlwaysSelectedEntryListWidget.Entry<Entry>() {
        private val issuesButton =
            if (musicPack?.validationMessages?.isEmpty() != false) {
                null
            }
            else {
                val result = ClickableTextDisplayWidget(issuesText.string)
                result.setTooltip(Tooltip.of(getValidationText(musicPack.validationMessages)))

                result
            }

        override fun render(
            context: DrawContext,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickDelta: Float
        ) {
            musicPack?.let {
                context.textConsumer.marqueedText(
                    Text.literal(it.packName),
                    x + 3,
                    x + 3,
                    rowRight - 3,
                    y + 3,
                    y + client.textRenderer.fontHeight + 3,
                )
                issuesButton?.let {
                    issuesButton.x = x + width - issuesButton.width - 5
                    issuesButton.y = y + height - issuesButton.height - 5
                    issuesButton.render(context, mouseX, mouseY, tickDelta)
                }
                context.textConsumer.marqueedText(
                    Text.literal(it.options.description).withColor(Colors.GRAY),
                    x + 3,
                    x + 3,
                    (issuesButton?.x ?: rowRight) - 3,
                    y + 17,
                    y + height
                )
            }

            if (musicPack == null) {
                context.drawText(
                    client.textRenderer,
                    Text.translatableWithFallback("trueadaptivemusic.vanilla", "Vanilla"),
                    x + 3,
                    y + 6,
                    Colors.WHITE,
                    false
                )
                context.drawText(
                    client.textRenderer,
                    Text.translatableWithFallback(
                        "trueadaptivemusic.disable_tam", "Disable TrueAdaptiveMusic"),
                    x + 3, y + 14 + 3,
                    Colors.GRAY,
                    false
                )
            }
        }

        override fun mouseClicked(click: Click, doubled: Boolean): Boolean {
            if (packListWidget.selectedOrNull == this) {
                return true
            }

            if (this.musicPack?.isValid == false)
            {
                return false
            }

            packListWidget.setSelected(this)
            onSelectPack(musicPack)

            return true
        }

        override fun getNarration(): Text {
            return Text.empty()
        }
    }
}