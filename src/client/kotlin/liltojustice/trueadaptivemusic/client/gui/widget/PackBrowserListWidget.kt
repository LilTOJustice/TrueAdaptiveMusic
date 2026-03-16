package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.CurlHelper
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.music.pack.BrowsableMusicPack
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.Click
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Colors
import kotlin.io.path.Path
import kotlin.io.path.invariantSeparatorsPathString

class PackBrowserListWidget(
    client: MinecraftClient,
    width: Int,
    height: Int,
    top: Int,
    itemHeight: Int,
    private val onSelectPack: (selectedPack: BrowsableMusicPack) -> Unit = {})
    : AlwaysSelectedEntryListWidget<PackBrowserListWidget.Entry>(client, width, height, top, itemHeight) {
    init {
        init()
    }

    fun init() {
        clearEntries()
        TAMClient.fetchPacksFromRepository()
            .forEach { musicPack ->
                addEntry(Entry(this, client, musicPack, onSelectPack))
            }
    }

    class Entry(
        private val packListWidget: PackBrowserListWidget,
        private val client: MinecraftClient,
        private val musicPack: BrowsableMusicPack,
        private val onSelectPack: (selectedPack: BrowsableMusicPack) -> Unit)
        : AlwaysSelectedEntryListWidget.Entry<Entry>() {
        private val downloadButton =
            ButtonWidget.Builder(DOWNLOAD_TEXT) {
                val curlUrl =
                    if (musicPack.source.startsWith(Constants.DISCORD_SOURCE_PREFIX))
                        musicPack.source
                    else if (musicPack.source.startsWith(Constants.DRIVE_SOURCE_PREFIX))
                        "${Constants.GOOGLE_DRIVE_LINK}/" +
                                musicPack.source.split("/").takeLast(2).first()
                    else
                        null

                curlUrl?.let {
                    CurlHelper.curl(
                        curlUrl,
                        Path(
                            Constants.MUSIC_PACK_DIR.invariantSeparatorsPathString,
                            "${musicPack.name}-${musicPack.version}.zip"
                        )
                    )
                }
            }
                .width(client.textRenderer.getWidth(DOWNLOAD_TEXT) + 5)
                .build()

        override fun render(
            context: DrawContext,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickDelta: Float
        ) {
            context.drawText(
                client.textRenderer, musicPack.name, x + 3, y + 6, Colors.WHITE, false)
            context.drawText(
                client.textRenderer,
                musicPack.description,
                x + 3, y + 14 + 3,
                Colors.GRAY,
                false
            )

            downloadButton.x = x + width - downloadButton.width - 5
            downloadButton.y = y + height - downloadButton.height - 5
            downloadButton.render(context, mouseX, mouseY, tickDelta)
        }

        override fun mouseClicked(click: Click, doubled: Boolean): Boolean {
            if (!isMouseOver(click.x, click.y)) {
                return false
            }

            downloadButton.mouseClicked(click, doubled)
            packListWidget.setSelected(this)
            onSelectPack(musicPack)

            return true
        }

        override fun getNarration(): Text {
            return Text.empty()
        }

        companion object {
            val DOWNLOAD_TEXT: MutableText = Text.translatableWithFallback(
                "trueadaptivemusic.download", "Download")
        }
    }
}