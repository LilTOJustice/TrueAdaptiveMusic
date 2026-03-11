package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.gui.widget.utility.InputWidgetMaker
import liltojustice.trueadaptivemusic.client.gui.widget.utility.WidgetMaker
import liltojustice.trueadaptivemusic.client.music.pack.MusicLoadException
import liltojustice.trueadaptivemusic.client.music.manager.MusicManager
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEventFactory
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEventRegistry
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicateFactory
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicateRegistry
import liltojustice.trueadaptivemusic.client.music.tree.MusicTree
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.toast.SystemToast
import net.minecraft.text.Text
import java.io.IOException
import kotlin.io.path.Path
import kotlin.io.path.pathString
import kotlin.reflect.KClass
import kotlin.reflect.KType

object TAMClient {
    val minecraftClient: MinecraftClient = MinecraftClient.getInstance()
    val predicateRegistry = MusicPredicateRegistry()
    val eventRegistry = MusicEventRegistry()
    val predicateFactory = MusicPredicateFactory(predicateRegistry)
    val eventFactory = MusicEventFactory(eventRegistry)
    val hasFFmpegGlobal
        get() =
            try { Runtime.getRuntime().exec(arrayOf("ffmpeg")).waitFor() in listOf(0, 1) }
            catch (_: IOException) { false }
    val hasFFmpegLocal
        get() =
            try {
                Runtime.getRuntime()
                    .exec(arrayOf(Constants.FFMPEG_PATH.pathString)).waitFor() in listOf(0, 1) &&
                        Runtime.getRuntime()
                            .exec(arrayOf(Constants.FFPROBE_PATH.pathString)).waitFor() in listOf(0, 1)
            } catch (_: IOException) {
                false
            }
    var hasFFmpeg = false
        private set

    var currentPredicateResult: MusicTree.Result? = null
    var options: TrueAdaptiveMusicOptions = TrueAdaptiveMusicOptions()
        set(value) {
            field = value
            options.save()
        }
    var musicPack: MusicPack? = null
        set(value) {
            field = value
            minecraftClient.soundManager.soundSystem.reloadSounds()
            hasFFmpeg = hasFFmpegGlobal || hasFFmpegLocal
            musicManager?.stop()

            val packName = value?.packName ?: ""
            try {
                options = options.copy(selectedPack = packName)
            } catch (_: IOException) {
                Logger.logError("Failed to save selected pack \"$packName\"")
            }
        }

    private val inputWidgetMaker = InputWidgetMaker()

    private var initialized = false
    private var musicManager: MusicManager? = null

    fun tick(client: MinecraftClient) {
        if (!initialized) {
            initialize(client)
        }

        musicPack?.let { pack ->
            currentPredicateResult = pack.rules.getMusicToPlay(minecraftClient)
            currentPredicateResult?.let { musicManager?.tick(it, pack.options) }
        } ?: { currentPredicateResult = null }
    }

    fun resetSound() {
        musicManager?.stop()
    }

    fun playSoundNow(sound: PlayableSound?) {
        musicManager?.playNow(sound)
    }

    fun getPlayingEvent(): MusicEvent? {
        return musicManager?.playingEvent
    }

    fun registerPredicate(name: String, triggerType: KClass<out MusicPredicate>) {
        predicateRegistry[name] = triggerType
    }

    fun registerEvent(name: String, triggerType: KClass<out MusicEvent>) {
        eventRegistry[name] = triggerType
    }

    @Suppress("unused")
    fun registerPredicate(name: String, triggerType: Class<out MusicPredicate>) {
        registerPredicate(name, triggerType.kotlin)
    }

    @Suppress("unused")
    fun registerEvent(name: String, triggerType: Class<out MusicEvent>) {
        registerEvent(name, triggerType.kotlin)
    }

    fun registerInputWidget(predicate: (parameterType: KType) -> Boolean, widgetMaker: WidgetMaker) {
        inputWidgetMaker.register(predicate, widgetMaker)
    }

    fun registerInputWidget(parameterType: KType, widgetMaker: WidgetMaker) {
        registerInputWidget({ type -> type == parameterType}, widgetMaker)
    }

    fun makeInputWidget(
        screen: Screen,
        outArgs: MutableList<Any?>,
        arg: InputWidgetMaker.WidgetArg,
        displayName: Text?,
        tooltipText: Text?,
        onChange: () -> Unit = {})
    : ClickableWidget {
        return inputWidgetMaker.makeWidget(screen, outArgs, arg, displayName, tooltipText, onChange)
    }

    fun refreshSoundVolume() {
        musicManager?.refreshSoundVolume()
    }

    fun <T: MusicEvent> invokeMusicEvent(eventType: KClass<T>, vararg eventArgs: Any?) {
        musicManager?.invokeMusicEvent(eventType, *eventArgs)
    }

    fun <T: MusicEvent> invokeMusicEvent(eventType: Class<T>, vararg eventArgs: Any?) {
        invokeMusicEvent(eventType.kotlin, *eventArgs)
    }

    private fun initialize(client: MinecraftClient) {
        if (initialized || !client.soundManager.soundSystem.started) {
            return
        }

        musicManager = MusicManager(client)

        options =
            try {
                TrueAdaptiveMusicOptions.jsonDecode(Constants.OPTIONS_PATH.toFile().readText())
            }
            catch (_: Exception) {
                Logger.logError("Failed to load TrueAdaptiveMusic settings. Resetting...")
                TrueAdaptiveMusicOptions()
            }

        try {
            musicPack =
                if (options.selectedPack.isBlank())
                    null
                else
                    MusicPack.fromFile(
                        Path(Constants.MUSIC_PACK_DIR.pathString, options.selectedPack))
        }
        catch (e: MusicLoadException) {
            Logger.logError("Selected pack \"${options.selectedPack}\" failed to load. Error:\n$e")
        }

        initialized = true
    }

    fun errorToast(errorMessage: Text, exceptionMessage: String? = null) {
        minecraftClient.toastManager.add(
            SystemToast.create(
                minecraftClient,
                SystemToast.Type.FILE_DROP_FAILURE,
                errorMessage,
                Text.literal(exceptionMessage ?: "")
            )
        )
    }
}