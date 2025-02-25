package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.GetMusicPackCallback
import liltojustice.trueadaptivemusic.client.MusicPack
import liltojustice.trueadaptivemusic.client.gui.widget.PackListWidget
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.Util
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.listDirectoryEntries

@Environment(EnvType.CLIENT)
class MainScreen(private val parent: Screen): Screen(Text.literal("True adaptive music")) {
    lateinit var createNewPackButton: ButtonWidget
    lateinit var packListWidget: PackListWidget
    lateinit var openMusicPacksButton: ButtonWidget

    override fun init() {
        createNewPackButton = ButtonWidget.Builder(Text.literal("Create a new music pack"))
        {
            val ongoingEdit = getOngoingEdit()
            if (ongoingEdit != null) {
                client?.setScreen(ConfirmBackupScreen(this, ongoingEdit, PackNameScreen(this)))
                return@Builder
            }

            client?.setScreen(PackNameScreen(this))
        }
            .build()

        openMusicPacksButton = ButtonWidget.Builder(OPEN_MUSIC_PACKS_TEXT) {
            Util.getOperatingSystem().open(Path(Constants.MUSIC_PACK_DIR).toUri())
        }
            .build()
        openMusicPacksButton.width = textRenderer.getWidth(OPEN_MUSIC_PACKS_TEXT) + 10
        openMusicPacksButton.x = width - openMusicPacksButton.width

        packListWidget = PackListWidget(
            client!!, this.width, this.height, 48, this.height - 64, 36)

        addSelectableChild(packListWidget)
        addDrawableChild(createNewPackButton)
    }

    override fun close() {
        client?.setScreen(parent)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        this.packListWidget.render(context, mouseX, mouseY, delta)
        super.render(context, mouseX, mouseY, delta)
    }

    companion object {
        fun getOngoingEdit(): Path? {
            return Path(Constants.MUSIC_PACK_DIR).listDirectoryEntries()
                .firstOrNull() { file -> file.extension == "new"}
        }

        fun getCurrentPack(): MusicPack? {
            val packResult = Array<MusicPack?>(1) { null }
            GetMusicPackCallback.EVENT.invoker().getPack(packResult)
            return packResult[0]
        }

        private val OPEN_MUSIC_PACKS_TEXT = Text.literal("Open Pack Folder")
    }
}