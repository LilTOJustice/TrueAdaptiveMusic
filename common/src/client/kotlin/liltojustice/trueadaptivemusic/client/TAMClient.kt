package liltojustice.trueadaptivemusic.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.Logger
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
import liltojustice.trueadaptivemusicapi.TrueAdaptiveMusicException
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.toasts.SystemToast
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvent
import net.minecraft.util.Util
import net.minecraft.util.Util.OS
import java.io.IOException
import java.nio.file.Path
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.pathString
import kotlin.time.Duration.Companion.milliseconds

object TAMClient {
    const val TPS = 20
    val musicPredicateFactory = MusicPredicateFactory()
    val musicEventFactory = MusicEventFactory()
    val platform = Util.getPlatform()
    var currentPredicateResult: MusicTree.Result? = null
        private set
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
    var extensions: Extensions? = null
        private set
    val hasFFmpeg get() = getFFmpeg()?.exists() == true && getFFProbe()?.exists() == true
    val allowedFileTypes
        get() =
            if (hasFFmpeg)
                Constants.ALL_ALLOWED_FILE_TYPES
            else
                listOf("ogg")

    private val TICK_MS = (1.0 / TPS * 1000).milliseconds
    private val minecraft: Minecraft = Minecraft.getInstance()

    private var musicManager: MusicManager? = null
    private lateinit var backgroundScope: CoroutineScope
    private var started = false
    private var modReflectionInterface: ClientModReflectionInterface? = null

    init {
        TAMAPI.registerEventListener { eventType, input ->
            musicManager?.invokeMusicEvent(eventType, input)
        }
    }

    @Suppress("UNNECESSARY_SAFE_CALL")
    fun initialize() {
        if (musicManager != null) {
            start()
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

    fun refreshSoundVolume() {
        musicManager?.refreshSoundVolume()
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

    fun getFFmpegCommand(): String? {
        return getFFmpeg()?.invariantSeparatorsPathString
    }

    fun getFFProbeCommand(): String? {
        return getFFProbe()?.invariantSeparatorsPathString
    }

    fun isCompatibilityMode(): Boolean {
        return musicManager?.isCompatibilityMode() ?: false
    }

    @Suppress("UNUSED")
    fun addExtensions(externalExtensions: Extensions) {
        extensions = externalExtensions
    }

    fun injectModReflectionInterface(clientModReflectionInterface: ClientModReflectionInterface) {
        modReflectionInterface = clientModReflectionInterface
    }

    fun getModReflectionInterface(): ClientModReflectionInterface {
        return modReflectionInterface
            ?: throw TrueAdaptiveMusicException("A client mod reflection interface has not been injected!")
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
        try {
            currentPredicateResult = musicPack?.let { pack ->
                val result = pack.rules.getMusicToPlay()
                musicManager?.tick(result, pack.options)

                result
            }
        }
        catch (e: Exception) {
            Logger.logWarning("True Adaptive Music manager encountered an error:\n${e.stackTraceToString()}")
        }
    }

    private fun getFFmpeg(): Path? {
        return when(platform) {
            OS.WINDOWS -> Constants.FFMPEG_WINDOWS_PATH
            OS.LINUX -> Constants.FFMPEG_PATH
            OS.OSX -> Constants.FFMPEG_PATH
            else -> null
        }
    }

    private fun getFFProbe(): Path? {
        return when(platform) {
            OS.WINDOWS -> Constants.FFPROBE_WINDOWS_PATH
            OS.LINUX -> Constants.FFPROBE_PATH
            OS.OSX -> Constants.FFPROBE_PATH
            else -> null
        }
    }
}