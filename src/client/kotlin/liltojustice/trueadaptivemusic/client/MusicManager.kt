package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicateTree
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.SimpleOption
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.sound.SoundInstance
import net.minecraft.registry.Registries
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

class MusicManager(
    private val client: MinecraftClient) {
    private var currentSoundPack: String = ""
    private var predicateTester: MusicPredicateTree? = null
    private var currentMusic: String = ""
    private var soundInstance: SoundInstance? = null
    private var oldSoundInstance: SoundInstance? = null
    private var toStop: SoundInstance? = null
    private var musicVolumeOption: SimpleOption<Double> = client.options.getSoundVolumeOption(SoundCategory.MUSIC)
    private val fadeInstances: MutableList<FadeInstance> = mutableListOf()

    init {
        client.soundManager.registerListener { instance, _ ->
            if (instance.category == SoundCategory.MUSIC && instance != soundInstance && instance != oldSoundInstance) {
                toStop = instance
                setInstanceVolume(toStop!!, 0F)
            }
        }
    }

    fun loadSoundPack(packPath: Path) {
        val predicateFile = packPath.toFile().listFiles()?.find { file -> file.name == Constants.RULES_FILENAME }
        if (predicateFile == null)
        {
            throw Exception("Expected file ${Constants.RULES_FILENAME} not found in pack path $packPath.")
        }

        stop()
        currentSoundPack = packPath.toFile().name
        predicateTester = MusicPredicateTree.fromJson(JsonHelper.deserialize(predicateFile.inputStream().reader()))
    }

    fun tick() {
        if (toStop != null) {
            client.soundManager.stop(toStop)
        }

        processFades()

        val musicPath: String = getNextMusic()
        if (!shouldPlay(musicPath))
        {
            return
        }

        println("starting new music from $musicPath")

        currentMusic = musicPath
        startNewMusic(musicPath)
    }

    private fun processFades() {
        fadeInstances.forEach { fadeInstance ->
            val volume: Float = fadeInstance.tick()
            setInstanceVolume(fadeInstance.soundInstance, musicVolumeOption.value.toFloat() * volume)
        }

        fadeInstances.removeIf { fadeinstance -> fadeinstance.done() }
    }

    private fun startNewMusic(musicPath: String) {
        if (musicPath == "")
        {
            if (client.soundManager.isPlaying(soundInstance)) {
                fadeInstances.add(FadeInstance(soundInstance!!, false))
            }

            return
        }

        var asPath: Path? = null
        try
        {
            asPath = Path("${Constants.MUSIC_PACK_DIR}/$currentSoundPack/$musicPath")
            if (asPath.isDirectory())
            {
                val music = asPath.toFile().listFiles()
                if (music!!.isEmpty()) {
                    throw Exception("No music found in directory $asPath.")
                }

                asPath = music.random().toPath()
            }
        }
        catch (_: Exception) {}

        var asSoundEvent: SoundEvent? = null
        try
        {
            asSoundEvent = Registries.SOUND_EVENT.get(Identifier(musicPath))
        }
        catch (_: Exception) {}

        if (asSoundEvent != null) {
            playNewSoundEvent(asSoundEvent)
        } else if (asPath != null && asPath.exists()) {
            playNewSoundFile(asPath)
        } else {
            throw Exception("Path $musicPath is neither a valid SoundEvent nor sound file.")
        }
    }

    private fun getNextMusic(): String {
        return predicateTester?.getMusicToPlay(client) ?: ""
    }

    private fun shouldPlay(musicPath: String): Boolean {
        return musicPath != currentMusic || !client.soundManager.isPlaying(soundInstance)
    }

    private fun stop() {
        if (soundInstance != null)
        {
            client.soundManager.stop(soundInstance)
            soundInstance = null
        }
    }

    private fun playNewSoundEvent(soundEvent: SoundEvent) {
        if (soundInstance == null) {
            soundInstance = PositionedSoundInstance.music(soundEvent)
            client.soundManager.play(soundInstance)
            return
        }

        beginCrossfade(PositionedSoundInstance.music(soundEvent))
    }

    private fun playNewSoundFile(soundFile: Path) {
        if (soundInstance == null) {
            soundInstance = AdaptiveMusicSoundInstance(soundFile)
            client.soundManager.play(soundInstance)
            return
        }

        beginCrossfade(AdaptiveMusicSoundInstance(soundFile))
    }

    private fun beginCrossfade(newSoundInstance: SoundInstance) {
        println("Beginning cross fade: $soundInstance -> $newSoundInstance")
        oldSoundInstance = soundInstance
        soundInstance = newSoundInstance
        client.soundManager.play(soundInstance)

        fadeInstances.add(FadeInstance(soundInstance!!, true))
        fadeInstances.add(FadeInstance(oldSoundInstance!!, false))
    }

    private fun setInstanceVolume(soundInstance: SoundInstance, volume: Float) {
        client.soundManager.soundSystem.sources[soundInstance]?.run { source ->
            source.setVolume(volume)
        }
    }
}