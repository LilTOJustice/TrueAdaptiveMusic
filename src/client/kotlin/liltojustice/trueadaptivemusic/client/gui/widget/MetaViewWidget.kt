package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.widget.utility.ContainerWidget
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import kotlin.reflect.full.primaryConstructor

class MetaViewWidget(initialMeta: MusicPack.Metadata, width: Int, height: Int, x: Int = 0, y: Int = 0)
    : ContainerWidget(width, height, "", false, false, x = x, y = y) {
    private val requiredMetaArgs = MusicPack.Metadata.getRequiredArgs()
    private var metaArgs: MutableList<Any?> = initialMeta.getArgs().toMutableList()

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        super.mouseClicked(mouseX, mouseY, button)
        return false
    }

    override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderWidget(context, mouseX, mouseY, delta)
        if (!visible) {
            return
        }

        requiredMetaArgs.forEach { required ->
            addWidgetFromRender(
                { TAMClient.makeInputWidget(screen!!, metaArgs, required) },
                "${required.name}: ${required.type}")
        }
    }

    fun getCurrentMeta(): MusicPack.Metadata {
        return MusicPack.Metadata::class.primaryConstructor?.call(*metaArgs.toTypedArray())
            ?: MusicPack.Metadata()
    }
}