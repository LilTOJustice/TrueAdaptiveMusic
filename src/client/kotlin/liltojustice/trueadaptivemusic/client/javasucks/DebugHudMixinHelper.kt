package liltojustice.trueadaptivemusic.client.javasucks

import liltojustice.trueadaptivemusic.client.TAMClient
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
            val text = MusicTrigger.getTruncatedTriggerId(path.last())

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
        val playingEvent = TAMClient.getPlayingEvent()
        playingEvent?.let {
            context.drawText(
                textRenderer,
                "${Text.translatableWithFallback(
                    "trueadaptivemusic.playing_event", "Playing event").string}: " +
                        playingEvent.getTriggerId(),
                1,
                1,
                Colors.WHITE,
                true
            )
            rowOffset += 2
        }

        predicateTreeLines.forEachIndexed { row, line ->
            val fontHeight = textRenderer.fontHeight
            val x: Int = line.indent * INDENT + 1
            val y: Int = (row + rowOffset) * (fontHeight + 2) + 1

            context.drawText(textRenderer, line.text, x, y, line.color, true)

            if (line.selected) {
                context.drawBorder(
                    x - 2,
                    y - 2,
                    textRenderer.getWidth(line.text) + 3,
                    fontHeight + 3, Colors.WHITE
                )
            }
        }
    }

    data class Line(val indent: Int, val text: String, val color: Int = Colors.WHITE, val selected: Boolean = false)
}

