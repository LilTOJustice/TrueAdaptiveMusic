package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.Callbacks
import liltojustice.trueadaptivemusic.client.MusicPack
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text

class PackListWidget(client: MinecraftClient, width: Int, height: Int, top: Int, bottom: Int, itemHeight: Int)
    : AlwaysSelectedEntryListWidget<PackListWidget.Entry>(client, width, height, top, bottom, itemHeight) {
    init {
        init()
    }

    fun init() {
        clearEntries()
        val vanillaEntry = Entry(this, client)
        addEntry(vanillaEntry)
        setSelected(vanillaEntry)
        MusicPack.loadAllPacks()
            .forEach { musicPack ->
                val newEntry = Entry(this, client, musicPack)
                addEntry(newEntry)
                if (musicPack.packName == Callbacks.getCurrentMusicPack()?.packName) {
                    setSelected(newEntry)
                }
            }
    }

    class Entry(
        private val packListWidget: PackListWidget,
        private val client: MinecraftClient,
        private val musicPack: MusicPack? = null)
        : AlwaysSelectedEntryListWidget.Entry<Entry>() {
        override fun render(
            context: MatrixStack?,
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
                drawCenteredTextWithShadow(
                    context,
                    client.textRenderer,
                    Text.of(it.packName).asOrderedText(),
                    x + 3,
                    y + 6,
                    0xffffff)
                drawCenteredTextWithShadow(context,
                    client.textRenderer,
                    Text.of(it.metadata.description).asOrderedText(),
                    x + 3, y + 14 + 3,
                    0x888888)
            }

            if (musicPack == null) {
                drawCenteredTextWithShadow(
                    context,
                    client.textRenderer,
                    Text.of("Vanilla").asOrderedText(),
                    x + 3,
                    y + 6,
                    0xffffff)
                drawCenteredTextWithShadow(
                    context,
                    client.textRenderer,
                    Text.of("Disable TrueAdaptiveMusic").asOrderedText(),
                    x + 3, y + 14 + 3,
                    0x888888)
            }
        }

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            if (packListWidget.selectedOrNull == this) {
                return true
            }
            
            packListWidget.setSelected(this)
            Callbacks.setCurrentMusicPack(musicPack)
            return true
        }

        override fun getNarration(): Text {
            return Text.of("")
        }
    }
}