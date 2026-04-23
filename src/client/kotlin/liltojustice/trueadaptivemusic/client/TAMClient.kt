package liltojustice.trueadaptivemusic.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.TrueAdaptiveMusic
import liltojustice.trueadaptivemusic.client.gui.screen.MissingPackBrowserScreen
import liltojustice.trueadaptivemusic.client.music.pack.MusicLoadException
import liltojustice.trueadaptivemusic.client.music.manager.MusicManager
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.music.tree.MusicTree
import liltojustice.trueadaptivemusic.client.sound.instance.TAMSoundInstance
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEventFactory
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicateFactory
import liltojustice.trueadaptivemusicapi.TAMAPI
import liltojustice.trueadaptivemusicapi.trigger.event.input.EmptyEventInput
import liltojustice.trueadaptivemusicapi.trigger.event.input.EventInput
import liltojustice.trueadaptivemusicapi.trigger.event.type.EventType
import liltojustice.trueadaptivemusicapi.widget.WidgetArg
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.toasts.SystemToast
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvent
import java.io.IOException
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.io.path.Path
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.pathString
import kotlin.time.Duration.Companion.milliseconds

object TAMClient {
    const val TPS = 20
    val TICK_MS = (1.0 / TPS * 1000).milliseconds
    val minecraftClient: Minecraft = Minecraft.getInstance()
    val musicPredicateFactory = MusicPredicateFactory()
    val musicEventFactory = MusicEventFactory()
    var currentPredicateResult: MusicTree.Result? = null
    var options: TrueAdaptiveMusicOptions = TrueAdaptiveMusicOptions()
        set(value) {
            field = value
            options.save()
        }
    var musicPack: MusicPack? = null
        set(value) {
            field = value
            minecraftClient.soundManager.soundEngine.reload()
            musicManager?.stop()

            val packName = value?.packName ?: ""
            try {
                options = options.copy(selectedPack = packName)
            } catch (_: IOException) {
                Logger.logError("Failed to save selected pack \"$packName\"")
            }
        }

    private val backgroundScope = CoroutineScope(EmptyCoroutineContext)
    private var initialized = false
    private var musicManager: MusicManager? = null
    private var packBrowserScreenProducer: ((Screen) -> Screen)? = null

    fun start() {
        val minecraft = Minecraft.getInstance()
        backgroundScope.launch {
            while (true) {
                try {
                    tick(minecraft)
                }
                catch (e: Exception) {
                    Logger.logError("TAM Processor thread encountered an error: ${e.message}\n" +
                            e.stackTraceToString())
                }

                delay(TICK_MS)
            }
        }
    }

    fun resetSound() {
        musicManager?.stop()
    }

    fun playSoundNow(sound: PlayableSound?) {
        musicManager?.playNow(sound)
    }

    fun getCurrentMusic(): TAMSoundInstance? {
        return musicManager?.currentMusic
    }

    fun getCurrentAmbience(): TAMSoundInstance? {
        return musicManager?.currentAmbience
    }

    fun getCurrentEventMusic(): TAMSoundInstance? {
        return musicManager?.currentEventMusic
    }

    fun getPlayingEvent(): MusicEvent<*>? {
        return musicManager?.playingEvent
    }

    fun makeInputWidget(
        screen: Screen,
        outArgs: MutableList<Any?>,
        arg: WidgetArg,
        displayName: Component?,
        tooltipText: Component?,
        onChange: () -> Unit = {}
    ): AbstractWidget {
        return TAMAPI.makeInputWidget(screen, outArgs, arg, displayName, tooltipText, onChange)
    }

    fun refreshSoundVolume() {
        musicManager?.refreshSoundVolume()
    }

    fun invokeMusicEvent(eventType: EventType<*, *, EmptyEventInput>) {
        musicManager?.invokeMusicEvent(eventType, EmptyEventInput())
    }

    fun <TInput: EventInput> invokeMusicEvent(eventType: EventType<*, *, TInput>, input: TInput) {
        musicManager?.invokeMusicEvent(eventType, input)
    }

    fun setDesiredVanillaSoundEvent(soundEvent: SoundEvent) {
        musicManager?.setDesiredVanillaSoundEvent(soundEvent)
    }

    fun errorToast(errorMessage: Component, exceptionMessage: String? = null) {
        minecraftClient.toastManager.addToast(
            SystemToast.multiline(
                minecraftClient,
                SystemToast.SystemToastId.FILE_DROP_FAILURE,
                errorMessage,
                Component.literal(exceptionMessage ?: "")
            )
        )
    }

    fun getFFProbeCommand(): String {
        return (if (TrueAdaptiveMusic.isWindows) Constants.FFPROBE_WINDOWS_PATH else Constants.FFPROBE_PATH)
            .invariantSeparatorsPathString
    }

    fun getFFmpegCommand(): String {
        return (if (TrueAdaptiveMusic.isWindows) Constants.FFMPEG_WINDOWS_PATH else Constants.FFMPEG_PATH)
            .invariantSeparatorsPathString
    }

    @Suppress("UNUSED")
    fun addPackBrowserSupport(screenProducer: (parent: Screen) -> Screen) {
        packBrowserScreenProducer = screenProducer
    }

    fun createPackBrowserScreen(parent: Screen): Screen {
        return packBrowserScreenProducer?.invoke(parent) ?: MissingPackBrowserScreen(parent)
    }

    private fun tick(minecraft: Minecraft) {
        if (!initialized) {
            initialize(minecraft)
        }

        currentPredicateResult = musicPack?.let { pack ->
            val result = pack.rules.getMusicToPlay(minecraftClient)
            musicManager?.tick(result, pack.options)

            result
        }
    }

    @Suppress("UNNECESSARY_SAFE_CALL")
    private fun initialize(minecraft: Minecraft) {
        if (initialized || minecraft.soundManager?.soundEngine?.loaded != true) {
            return
        }

        musicManager = MusicManager(minecraft)

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
}