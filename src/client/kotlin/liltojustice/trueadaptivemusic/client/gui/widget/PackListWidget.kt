package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.music.MusicPack
import liltojustice.trueadaptivemusic.client.music.MusicPackValidation
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget
import net.minecraft.client.gui.widget.ButtonWidget
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

    class Entry(
        private val packListWidget: PackListWidget,
        private val client: MinecraftClient,
        private val musicPack: MusicPack?,
        private val onSelectPack: (selectedPack: MusicPack?) -> Unit)
        : AlwaysSelectedEntryListWidget.Entry<Entry>() {
        private val issuesButton =
            if (musicPack?.validationMessages?.isEmpty() != false)
                null
            else
                ButtonWidget.Builder(issuesText) {}
                .tooltip(Tooltip.of(getValidationText(musicPack.validationMessages)))
                .width(client.textRenderer.getWidth(issuesText) + 5)
                .build()

        override fun render(
            context: DrawContext?,
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
            musicPack?.let {
                context?.drawText(
                    client.textRenderer, it.packName, x + 3, y + 6, Colors.WHITE, false)
                context?.drawText(
                    client.textRenderer,
                    it.metadata.description,
                    x + 3, y + 14 + 3,
                    Colors.GRAY,
                    false)

                issuesButton?.let {
                    issuesButton.x = x + entryWidth - issuesButton.width - 5
                    issuesButton.y = y + entryHeight - issuesButton.height - 5
                    issuesButton.render(context, mouseX, mouseY, tickDelta)
                }
            }

            if (musicPack == null) {
                context?.drawText(
                    client.textRenderer,
                    Text.translatableWithFallback("trueadaptivemusic.vanilla", "Vanilla"),
                    x + 3,
                    y + 6,
                    Colors.WHITE,
                    false)
                context?.drawText(
                    client.textRenderer,
                    Text.translatableWithFallback(
                        "trueadaptivemusic.disable_tam", "Disable TrueAdaptiveMusic"),
                    x + 3, y + 14 + 3,
                    Colors.GRAY,
                    false)
            }
        }

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
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

        companion object {
            private val issuesText = Text.translatableWithFallback(
                "trueadaptivemusic.issues_found", "Issues Found")
            private fun getValidationText(validation: List<MusicPackValidation.ValidationMessage>): Text {
                val warnings = validation.filter { it.type == MusicPackValidation.ValidationMessage.Type.Warning }
                val errors = validation.filter { it.type == MusicPackValidation.ValidationMessage.Type.Error }
                val result = StringBuilder()
                if (warnings.isNotEmpty()) {
                    result.append(
                        Text.translatableWithFallback(
                            "trueadaptivemusic.warning_count",
                            "%i warning(s)",
                            warnings.size))
                }

                if (warnings.isNotEmpty() && errors.isNotEmpty()) {
                    result.append(" ${Text.translatableWithFallback("trueadaptivemusic.and", "and")} ")
                }

                if (errors.isNotEmpty()) {
                    result.append(
                        Text.translatableWithFallback(
                            "trueadaptivemusic.error_count",
                            "%i error(s)",
                            errors.size))
                }

                if (warnings.isNotEmpty() || errors.isNotEmpty()) {
                    result.appendLine()
                    result.appendLine()
                }

                result.append(validation.joinToString("\n\n") { message -> message.toString() })

                return Text.literal(result.toString())
            }
        }
    }
}