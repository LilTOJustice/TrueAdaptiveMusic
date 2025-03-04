package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.Callbacks
import liltojustice.trueadaptivemusic.client.ChangeMusicPackCallback
import liltojustice.trueadaptivemusic.client.gui.widget.PackListWidget
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.screen.ScreenTexts
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Util
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

@Environment(EnvType.CLIENT)
class MainScreen(private val parent: Screen): Screen(Text.literal("Music Packs")) {
    private lateinit var createNewPackButton: ButtonWidget
    private lateinit var packListWidget: PackListWidget
    private lateinit var openMusicPacksButton: ButtonWidget
    private lateinit var doneButton: ButtonWidget
    private lateinit var editButton: ButtonWidget
    private lateinit var refreshButton: ButtonWidget

    override fun init() {
        ChangeMusicPackCallback.EVENT.register { musicPack ->
            editButton.visible = musicPack != null
            return@register ActionResult.PASS
        }

        createNewPackButton = ButtonWidget.Builder(CREATE_PACK_TEXT)
        {
            val ongoingEdit = getOngoingEdit()
            if (ongoingEdit != null) {
                client?.setScreen(ConfirmBackupScreen(this, ongoingEdit, PackNameScreen(this)))
                return@Builder
            }

            client?.setScreen(PackNameScreen(this))
        }
            .build()
        createNewPackButton.width = textRenderer.getWidth(CREATE_PACK_TEXT) + 10

        openMusicPacksButton = ButtonWidget.Builder(OPEN_MUSIC_PACKS_TEXT) {
            Util.getOperatingSystem().open(Path(Constants.MUSIC_PACK_DIR).toUri())
        }
            .build()
        openMusicPacksButton.width = textRenderer.getWidth(OPEN_MUSIC_PACKS_TEXT) + 10
        openMusicPacksButton.x = width - openMusicPacksButton.width

        packListWidget = PackListWidget(
            client!!, this.width, this.height, 48, this.height - 64, 36)

        doneButton = ButtonWidget.builder(ScreenTexts.DONE) { _: ButtonWidget? -> client?.setScreen(parent) }
            .build()
        doneButton.width = textRenderer.getWidth(ScreenTexts.DONE) + 10
        doneButton.x = width - doneButton.width
        doneButton.y = height - doneButton.height

        editButton = ButtonWidget.Builder(EDIT_TEXT)
        {
            val currentPack = Callbacks.getCurrentMusicPack()
            val ongoingEdit = getOngoingEdit()
            val editScreen = EditPackScreen(this, currentPack ?: return@Builder)
            if (ongoingEdit != null && ongoingEdit.name != currentPack.packName) {
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
        editButton.width = textRenderer.getWidth(EDIT_TEXT) + 10
        editButton.y = height - editButton.height
        editButton.visible = Callbacks.getCurrentMusicPack() != null

        refreshButton = ButtonWidget.builder(REFRESH_TEXT) { _: ButtonWidget? -> reload() }
            .build()
        refreshButton.y = createNewPackButton.y + createNewPackButton.height + 5
        refreshButton.width = textRenderer.getWidth(REFRESH_TEXT) + 10

        addSelectableChild(packListWidget)
        addDrawableChild(createNewPackButton)
        addDrawableChild(openMusicPacksButton)
        addDrawableChild(doneButton)
        addDrawableChild(editButton)
        addDrawableChild(refreshButton)
    }

    override fun close() {
        client?.setScreen(parent)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        this.packListWidget.render(context, mouseX, mouseY, delta)
        context?.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 16777215)
        super.render(context, mouseX, mouseY, delta)
    }

    fun reload() {
        packListWidget.init()
    }

    companion object {
        fun getOngoingEdit(): Path? {
            return Path(Constants.MUSIC_PACK_DIR).listDirectoryEntries()
                .firstOrNull() { file -> file.extension == "new"}
        }

        private val OPEN_MUSIC_PACKS_TEXT = Text.literal("Open Pack Folder")
        private val CREATE_PACK_TEXT = Text.literal("Create a new music pack")
        private val REFRESH_TEXT = Text.literal("Refresh")
        private val EDIT_TEXT = Text.literal("Edit Pack")
    }
}