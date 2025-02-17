package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.GetMusicPackCallback
import liltojustice.trueadaptivemusic.client.MusicPack
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.listDirectoryEntries

@Environment(EnvType.CLIENT)
class MainScreen(private val parent: Screen): Screen(Text.literal("True adaptive music")) {
    override fun init() {
        val packResult = Array<MusicPack?>(1) { null }
        GetMusicPackCallback.EVENT.invoker().getPack(packResult)
        val createNewPackButton = ButtonWidget.Builder(Text.literal("Create a new music pack"))
        {
            val ongoingEdit = getOngoingEdit()
            if (ongoingEdit != null) {
                client?.setScreen(ConfirmBackupScreen(this, ongoingEdit, PackNameScreen(this)))
                return@Builder
            }

            client?.setScreen(PackNameScreen(this))
        }
            .build()
        val editCurrentPackButton = ButtonWidget.Builder(Text.literal("Edit current pack"))
        {
            val ongoingEdit = getOngoingEdit()
            val editScreen = EditPackScreen(this, packResult[0]?.copy() ?: return@Builder)
            if (ongoingEdit != null) {
                client?.setScreen(
                    ConfirmBackupScreen(
                        this,
                        ongoingEdit,
                        editScreen))
                return@Builder
            }

            client?.setScreen(editScreen)
        }
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

        fun getOngoingEdit(): Path? {
            return Path(Constants.MUSIC_PACK_DIR).listDirectoryEntries()
                .firstOrNull() { file -> file.extension == "new"}
        }
    }
}