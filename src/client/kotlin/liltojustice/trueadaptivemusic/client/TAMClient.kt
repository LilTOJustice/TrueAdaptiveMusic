package liltojustice.trueadaptivemusic.client

import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.CurlHelper
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.TrueAdaptiveMusic
import liltojustice.trueadaptivemusic.client.gui.widget.utility.InputWidgetMaker
import liltojustice.trueadaptivemusic.client.gui.widget.utility.WidgetMaker
import liltojustice.trueadaptivemusic.client.music.pack.MusicLoadException
import liltojustice.trueadaptivemusic.client.music.manager.MusicManager
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import liltojustice.trueadaptivemusic.client.music.pack.browsable.BrowsableMusicPack
import liltojustice.trueadaptivemusic.client.music.pack.browsable.PackManifest
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEventFactory
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEventRegistry
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicateFactory
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicateRegistry
import liltojustice.trueadaptivemusic.client.music.tree.MusicTree
import liltojustice.trueadaptivemusic.client.serialization.EnumTypeAdapter
import liltojustice.trueadaptivemusic.client.sound.instance.TAMSoundInstance
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.toasts.SystemToast
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvent
import java.io.IOException
import java.util.Calendar
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.moveTo
import kotlin.io.path.pathString
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.time.Duration.Companion.milliseconds

object TAMClient {
    const val TPS = 20
    val TICK_MS = (1.0 / TPS * 1000).milliseconds
    val minecraftClient: Minecraft = Minecraft.getInstance()
    val predicateRegistry = MusicPredicateRegistry()
    val eventRegistry = MusicEventRegistry()
    val predicateFactory = MusicPredicateFactory(predicateRegistry)
    val eventFactory = MusicEventFactory(eventRegistry)
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
    private val inputWidgetMaker = InputWidgetMaker()
    private var initialized = false
    private var musicManager: MusicManager? = null

    fun start() {
        val client = Minecraft.getInstance()
        backgroundScope.launch {
            while (true) {
                try {
                    tick(client)
                }
                catch (e: Exception) {
                    Logger.logError("TAM Processor thread encountered an error: ${e.message}\n" +
                            e.stackTraceToString()
                    )
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
        displayName: Component?,
        tooltipText: Component?,
        onChange: () -> Unit = {}
    ): AbstractWidget {
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

    suspend fun fetchPacksFromRepository(ignoreCache: Boolean = false): PackManifest? {
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .registerTypeAdapter(
                BrowsableMusicPack.SourceType::class.java,
                EnumTypeAdapter(BrowsableMusicPack.SourceType::class)
            ).create()
        if (!ignoreCache && Constants.MANIFEST_PATH.exists()) {
            return gson
                .fromJson(Constants.MANIFEST_PATH.toFile().readText(), PackManifest::class.java)
        }

        coroutineScope { CurlHelper.curl(Constants.MANIFEST_FILE_URL, Constants.MANIFEST_PATH_TEMP) }

        if (!Constants.MANIFEST_PATH_TEMP.exists()) {
            Logger.logError("Failed to fetch pack manifest.")

            return null
        }

        val manifest = try {
            gson
                .fromJson(Constants.MANIFEST_PATH_TEMP.toFile().readText(), PackManifest::class.java)
                .copy(timestamp = Calendar.getInstance().time)
        }
        catch (_: JsonSyntaxException) {
            Logger.logError("Failed to parse manifest json.")

            return null
        }

        Constants.MANIFEST_PATH_TEMP.moveTo(Constants.MANIFEST_PATH, true)
        Constants.MANIFEST_PATH.toFile().writeText(gson.toJson(manifest))

        manifest.packs.forEach { pack ->
            pack.getImagePath()?.let { imagePath ->
                pack.image?.source?.let { source ->
                    CurlHelper.curl(source, imagePath)
                }
            }
        }

        return manifest
    }

    private fun tick(client: Minecraft) {
        if (!initialized) {
            initialize(client)
        }

        currentPredicateResult = musicPack?.let { pack ->
            val result = pack.rules.getMusicToPlay(minecraftClient)
            musicManager?.tick(result, pack.options)

            result
        }
    }

    @Suppress("UNNECESSARY_SAFE_CALL")
    private fun initialize(client: Minecraft) {
        if (initialized || client.soundManager?.soundEngine?.loaded != true) {
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
}