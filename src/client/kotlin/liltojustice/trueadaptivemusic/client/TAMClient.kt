package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.TrueAdaptiveMusicOptions
import liltojustice.trueadaptivemusic.client.gui.widget.utility.InputWidgetMaker
import liltojustice.trueadaptivemusic.client.music.MusicLoadException
import liltojustice.trueadaptivemusic.client.music.MusicManager
import liltojustice.trueadaptivemusic.client.music.MusicPack
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEventFactory
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEventRegistry
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicateFactory
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicateRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.sound.SoundInstance
import java.io.IOException
import kotlin.io.path.Path
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType

object TAMClient {
    private var initialized = false
    private var musicManager: MusicManager? = null
    val predicateRegistry = MusicPredicateRegistry()
    val eventRegistry = MusicEventRegistry()
    val predicateFactory = MusicPredicateFactory(predicateRegistry)
    val eventFactory = MusicEventFactory(eventRegistry)
    private val inputWidgetMaker = InputWidgetMaker()

    var options: TrueAdaptiveMusicOptions = TrueAdaptiveMusicOptions()
        set(value) {
            field = value
            options.save()
        }

    var musicPack: MusicPack?
        get() = musicManager?.getMusicPack()
        set(value) {
            musicManager?.selectMusicPack(value)

            val packName = value?.packName ?: ""
            try {
                options = options.copy(selectedPack = packName)
            } catch (_: IOException) {
                Logger.logError("Failed to save selected pack \"$packName\"")
            }
        }

    fun tick(client: MinecraftClient) {
        if (!initialized) {
            initialize(client)
        }

        musicManager!!.tick()
    }

    fun playSoundNow(sound: PlayableSound?, keepBackground: Boolean = false) {
        musicManager?.playNow(sound, keepBackground)
    }

    fun refreshCurrentMusicPack() {
        musicPack = musicPack
    }

    fun getPlayingEvent(): MusicEvent? {
        return musicManager?.playingEvent
    }

    fun hasSoundInstance(instance: SoundInstance): Boolean {
        return musicManager?.hasSoundInstance(instance) ?: false
    }

    fun registerPredicate(name: String, triggerType: KClass<out MusicPredicate>) {
        predicateRegistry[name] = triggerType
    }

    fun registerEvent(name: String, triggerType: KClass<out MusicEvent>) {
        eventRegistry[name] = triggerType
    }

    fun registerPredicate(name: String, triggerType: Class<out MusicPredicate>) {
        predicateRegistry[name] = triggerType
    }

    fun registerEvent(name: String, triggerType: Class<out MusicEvent>) {
        eventRegistry[name] = triggerType
    }

    fun registerInputWidget(
        predicate: (parameterType: KType) -> Boolean,
        widgetMaker: (prompt: String, screen: Screen, outArgs: MutableList<Any?>, arg: KParameter) -> ClickableWidget) {
        inputWidgetMaker.register(predicate, widgetMaker)
    }

    fun registerInputWidget(
        parameterType: KType,
        widgetMaker: (prompt: String, screen: Screen, outArgs: MutableList<Any?>, arg: KParameter) -> ClickableWidget) {
        registerInputWidget({ type -> type == parameterType}, widgetMaker)
    }

    fun makeInputWidget(screen: Screen, outArgs: MutableList<Any?>, arg: KParameter): ClickableWidget {
        return inputWidgetMaker.makeWidget(screen, outArgs, arg)
    }

    private fun initialize(client: MinecraftClient) {
        if (initialized) {
            return
        }

        musicManager = MusicManager(client)

        options =
            try {
                TrueAdaptiveMusicOptions.jsonDecode(Path(Constants.OPTIONS_FILENAME).toFile().readText())
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
                    MusicPack.fromFile(Path(Constants.MUSIC_PACK_DIR, options.selectedPack))
        }
        catch (e: MusicLoadException) {
            Logger.logError("Selected pack \"${options.selectedPack}\" failed to load. Error:\n$e")
        }

        initialized = true
    }
}