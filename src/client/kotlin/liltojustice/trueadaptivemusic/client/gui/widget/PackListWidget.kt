package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.Callbacks
import liltojustice.trueadaptivemusic.client.MusicPack
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget
import net.minecraft.text.Text
import net.minecraft.util.Colors

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
            }

            if (musicPack == null) {
                context?.drawText(
                    client.textRenderer, "Vanilla", x + 3, y + 6, Colors.WHITE, false)
                context?.drawText(
                    client.textRenderer,
                    "Disable TrueAdaptiveMusic",
                    x + 3, y + 14 + 3,
                    Colors.GRAY,
                    false)
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
            return Text.empty()
        }
    }
}