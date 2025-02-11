package liltojustice.trueadaptivemusic.client.gui.screen

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text

@Environment(EnvType.CLIENT)
class MainScreen(private val parent: Screen): Screen(Text.literal("True adaptive music")) {
    private lateinit var createNewPackButton: ButtonWidget

    override fun init() {
        createNewPackButton = ButtonWidget.Builder(Text.literal("Create a new music pack"))
        { client?.setScreen(EditPackScreen(this)) }
            .build()

        addDrawableChild(createNewPackButton)
    }

    override fun close() {
        client?.setScreen(parent)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
    }

    companion object {
        fun getTrueAdaptiveMusicButton(client: MinecraftClient?, parent: Screen): ButtonWidget {
            return ButtonWidget.Builder(Text.literal("True Adaptive Music"))
            {
                client?.setScreen(MainScreen(parent))
            }
                .build()
        }
    }
}