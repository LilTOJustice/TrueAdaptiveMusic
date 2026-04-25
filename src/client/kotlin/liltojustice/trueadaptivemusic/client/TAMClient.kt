package liltojustice.trueadaptivemusic.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
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
    val minecraft: Minecraft = Minecraft.getInstance()
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
            resetSound()

            val packName = value?.packName ?: ""
            try {
                options = options.copy(selectedPack = packName)
            } catch (_: IOException) {
                Logger.logError("Failed to save selected pack \"$packName\"")
            }
        }

    private lateinit var backgroundScope: CoroutineScope
    private var musicManager: MusicManager? = null
    private var packBrowserScreenProducer: ((Screen) -> Screen)? = null
    private var started = false

    @Suppress("UNNECESSARY_SAFE_CALL")
    fun initialize() {
        if (musicManager != null) {
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

        start()
    }

    fun stop() {
        if (!started) {
            return
        }

        musicManager?.stop()
        backgroundScope.cancel()
        started = false
    }

    fun resetSound() {
        stop()
        start()
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
        minecraft.toastManager.addToast(
            SystemToast.multiline(
                minecraft,
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

    private fun start() {
        if (started) {
            return
        }

        minecraft.musicManager.stopPlaying()
        backgroundScope = CoroutineScope(EmptyCoroutineContext)
        backgroundScope.launch {
            while (true) {
                try {
                    tick()
                }
                catch (e: Exception) {
                    Logger.logError("TAM Processor thread encountered an error:\n${e.stackTraceToString()}")
                }

                delay(TICK_MS)
            }
        }

        started = true
    }

    private fun tick() {
        currentPredicateResult = musicPack?.let { pack ->
            val result = pack.rules.getMusicToPlay()
            musicManager?.tick(result, pack.options)

            result
        }
    }
}