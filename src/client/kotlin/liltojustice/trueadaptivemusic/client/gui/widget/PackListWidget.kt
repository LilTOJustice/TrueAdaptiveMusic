package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.MusicPack
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget
import net.minecraft.text.Text
import net.minecraft.util.Colors

class PackListWidget(client: MinecraftClient, width: Int, height: Int, top: Int, bottom: Int, itemHeight: Int)
    : AlwaysSelectedEntryListWidget<PackListWidget.Entry>(client, width, height, top, bottom, itemHeight) {
    init {
        MusicPack.loadAllPacks()
            .forEach { musicPack -> addEntry(Entry(this, client, musicPack)) }
    }

    class Entry(
        private val packListWidget: PackListWidget,
        private val client: MinecraftClient,
        private val musicPack: MusicPack)
        : AlwaysSelectedEntryListWidget.Entry<Entry>() {
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
            context?.drawText(
                client.textRenderer, musicPack.packName, x + 3, y + 6, Colors.WHITE, false)
            context?.drawText(
                client.textRenderer,
                musicPack.metadata.description,
                x + 3, y + 14 + 3,
                Colors.GRAY,
                false)
        }

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            packListWidget.setSelected(this)
            return true
        }

        override fun getNarration(): Text {
            return Text.empty()
        }
    }
}