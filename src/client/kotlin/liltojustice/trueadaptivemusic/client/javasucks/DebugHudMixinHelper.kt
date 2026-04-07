package liltojustice.trueadaptivemusic.client.javasucks

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.extensions.drawBorder
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import liltojustice.trueadaptivemusic.client.music.tree.MusicTree
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors

object DebugHudMixinHelper {
    private const val INDENT = 10

    @JvmStatic
    fun render(graphics: GuiGraphicsExtractor) {
        if (!TAMClient.options.useDebugHud) {
            return
        }

        val musicPack = TAMClient.musicPack ?: return

        val minecraft = Minecraft.getInstance()
        if (minecraft.gui.debugOverlay.showDebugScreen()) {
            return
        }

        val font = minecraft.font
        val predicateTreeLines = mutableListOf<Line>()
        val rules = musicPack.rules
        val currentNodePath = TAMClient.currentPredicateResult?.path ?: return
        val currentNodePathElements = currentNodePath.split(MusicTree.PATH_SEPARATOR)
        val currentNodeDepth = currentNodePathElements.size

        rules.preorderTraverse { _, path ->
            val text = MusicTrigger.getTruncatedTriggerId(path.last()).takeIf { it.isNotEmpty() } ?: "empty"

            if (path.all { pathElement -> currentNodePathElements.contains(pathElement) }) {
                predicateTreeLines.add(
                    Line(
                        path.size - 1,
                        text,
                        CommonColors.GREEN,
                        currentNodeDepth == path.size
                    )
                )
            }
            else if (path.size <= currentNodeDepth) {
                predicateTreeLines.add(Line(path.size - 1, text))
            }
            else if (path.size - 1 == currentNodeDepth) {
                predicateTreeLines.add(Line(path.size - 1, text))
            }
            else if (path.size - 2 == currentNodeDepth) {
                predicateTreeLines.add(
                    Line(path.size - 1, text.replace(Regex("\\{.*}"), "{...}")))
            }
            else if (path.size - 3 == currentNodeDepth) {
                predicateTreeLines.add(Line(path.size - 1, "..."))
            }
        }

        var rowOffset = 0
        val fontHeight = font.lineHeight
        val playingEvent = TAMClient.getPlayingEvent()
        val eventMusic = TAMClient.getCurrentEventMusic()
        playingEvent?.let {
            graphics.text(
                font,
                "${
                    Component.translatableWithFallback(
                    "trueadaptivemusic.playing_event", "Playing event").string}: ${it.getTriggerId()} " +
                        "(${eventMusic?.getSoundString()})",
                1,
                getY(rowOffset++, fontHeight),
                CommonColors.WHITE,
                true
            )
        }

        val playingMusic = TAMClient.getCurrentMusic()
        playingMusic?.let {
            graphics.text(
                font,
                "${
                    Component.translatableWithFallback(
                    "trueadaptivemusic.playing_music", "Playing music").string}: ${it.getSoundString()}",
                1,
                getY(rowOffset++, fontHeight),
                CommonColors.WHITE,
                true
            )
        }

        val playingAmbience = TAMClient.getCurrentAmbience()
        playingAmbience?.let {
            graphics.text(
                font,
                "${
                    Component.translatableWithFallback(
                    "trueadaptivemusic.playing_ambience", "Playing ambience").string}: " +
                        it.getSoundString(),
                1,
                getY(rowOffset++, fontHeight),
                CommonColors.WHITE,
                true
            )
        }

        if (playingEvent != null || playingMusic != null || playingAmbience != null) {
            rowOffset++
        }

        predicateTreeLines.forEachIndexed { row, line ->
            val x: Int = line.indent * INDENT + 1
            val y: Int = getY(row + rowOffset, fontHeight)

            graphics.text(font, line.text, x, y, line.color, true)

            if (line.selected) {
                graphics.drawBorder(
                    x - 2, y - 2, font.width(line.text) + 3, fontHeight + 3)
            }
        }
    }

    private fun getY(row: Int, fontHeight: Int): Int {
        return row * (fontHeight + 2) + 1
    }

    private data class Line(
        val indent: Int, val text: String, val color: Int = CommonColors.WHITE, val selected: Boolean = false)
}

