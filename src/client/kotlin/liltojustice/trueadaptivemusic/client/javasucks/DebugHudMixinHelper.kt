package liltojustice.trueadaptivemusic.client.javasucks

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicateTree
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.Colors

class DebugHudMixinHelper {
    companion object {
        private const val INDENT = 10

        @JvmStatic
        fun render(context: DrawContext) {
            if (!TAMClient.options.useDebugHud) {
                return
            }

            val musicPack = TAMClient.musicPack ?: return

            val client = MinecraftClient.getInstance()
            if (client.options.debugEnabled) {
                return
            }

            val textRenderer = client.textRenderer
            val predicateTreeLines = mutableListOf<Line>()
            val rules = musicPack.rules
            val currentNodePath = rules.getMusicToPlay(client).path
            val currentNodeDepth = currentNodePath.split(MusicPredicateTree.PATH_SEPARATOR).size

            rules.preorderTraverse { _, path ->
                val pathString = path.joinToString(MusicPredicateTree.PATH_SEPARATOR)
                val text = MusicTrigger.getTruncatedTriggerId(path.last())

                if (currentNodePath.contains(pathString)) {
                    predicateTreeLines.add(Line(path.size - 1, pathString, text, Constants.Colors.GREEN))
                }
                else if (path.size <= currentNodeDepth) {
                    predicateTreeLines.add(Line(path.size - 1, pathString, text))
                }
                else if (path.size - 1 == currentNodeDepth) {
                    predicateTreeLines.add(Line(path.size - 1, pathString, text))
                }
                else if (path.size - 2 == currentNodeDepth) {
                    predicateTreeLines.add(
                        Line(path.size - 1, pathString, text.replace(Regex("\\{.*}"), "{...}")))
                }
                else if (path.size - 3 == currentNodeDepth) {
                    predicateTreeLines.add(Line(path.size - 1, pathString, "..."))
                }
            }

            var rowOffset = 0
            val playingEvent = TAMClient.getPlayingEvent()
            playingEvent?.let {
                context.drawText(
                    textRenderer,
                    "Playing event: ${playingEvent.getTriggerId()}",
                    1,
                    1,
                    Colors.WHITE,
                    true)
                rowOffset += 2
            }

            predicateTreeLines.forEachIndexed { row, line ->
                val fontHeight = textRenderer.fontHeight
                val x: Int = line.indent * INDENT + 1
                val y: Int = (row + rowOffset) * (fontHeight + 2) + 1

                context.drawText(textRenderer, line.text, x, y, line.color, true)

                if (line.path == currentNodePath) {
                    context.drawBorder(
                        x - 2,
                        y - 2,
                        textRenderer.getWidth(line.text) + 3,
                        fontHeight + 3,
                        Colors.WHITE)
                }
            }
        }
    }

    data class Line(val indent: Int, val path: String, val text: String, val color: Int = Colors.WHITE)
}
