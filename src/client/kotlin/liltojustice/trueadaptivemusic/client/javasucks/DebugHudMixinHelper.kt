package liltojustice.trueadaptivemusic.client.javasucks

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.extensions.drawBorder
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import liltojustice.trueadaptivemusic.client.music.tree.MusicTree
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import net.minecraft.util.Colors

object DebugHudMixinHelper {
    private const val INDENT = 10

    @JvmStatic
    fun render(context: DrawContext) {
        if (!TAMClient.options.useDebugHud) {
            return
        }

        val musicPack = TAMClient.musicPack ?: return

        val client = MinecraftClient.getInstance()
        if (client.inGameHud.debugHud.shouldShowDebugHud()) {
            return
        }

        val textRenderer = client.textRenderer
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
                        Colors.GREEN,
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
        val fontHeight = textRenderer.fontHeight
        val playingEvent = TAMClient.getPlayingEvent()
        val eventMusic = TAMClient.getCurrentEventMusic()
        playingEvent?.let {
            context.drawText(
                textRenderer,
                "${Text.translatableWithFallback(
                    "trueadaptivemusic.playing_event", "Playing event").string}: ${it.getTriggerId()} " +
                        "(${eventMusic?.getSoundName()})",
                1,
                getY(rowOffset++, fontHeight),
                Colors.WHITE,
                true
            )
        }

        val playingMusic = TAMClient.getCurrentMusic()
        playingMusic?.let {
            context.drawText(
                textRenderer,
                "${Text.translatableWithFallback(
                    "trueadaptivemusic.playing_music", "Playing music").string}: ${it.getSoundName()}",
                1,
                getY(rowOffset++, fontHeight),
                Colors.WHITE,
                true
            )
        }

        val playingAmbience = TAMClient.getCurrentAmbience()
        playingAmbience?.let {
            context.drawText(
                textRenderer,
                "${Text.translatableWithFallback(
                    "trueadaptivemusic.playing_ambience", "Playing ambience").string}: " +
                        it.getSoundName(),
                1,
                getY(rowOffset++, fontHeight),
                Colors.WHITE,
                true
            )
        }

        if (playingEvent != null || playingMusic != null || playingAmbience != null) {
            rowOffset++
        }

        predicateTreeLines.forEachIndexed { row, line ->
            val x: Int = line.indent * INDENT + 1
            val y: Int = getY(row + rowOffset, fontHeight)

            context.drawText(textRenderer, line.text, x, y, line.color, true)

            if (line.selected) {
                context.drawBorder(
                    x - 2, y - 2, textRenderer.getWidth(line.text) + 3, fontHeight + 3)
            }
        }
    }

    private fun getY(row: Int, fontHeight: Int): Int {
        return row * (fontHeight + 2) + 1
    }

    private data class Line(val indent: Int, val text: String, val color: Int = Colors.WHITE, val selected: Boolean = false)
}

