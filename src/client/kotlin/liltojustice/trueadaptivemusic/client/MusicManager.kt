package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.LogLevel
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicateTree
import liltojustice.trueadaptivemusic.client.predicate.RulesParserException
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.SimpleOption
import net.minecraft.client.sound.SoundInstance
import net.minecraft.sound.SoundCategory
import net.minecraft.util.JsonHelper
import java.nio.file.Path

class MusicManager(
    private val client: MinecraftClient) {
    private var predicateTester: MusicPredicateTree? = null
    private var currentMusicPredId: String = ""
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
        try {
            predicateTester = MusicPredicateTree
                .fromJson(JsonHelper.deserialize(predicateFile.inputStream().reader()), packPath.toFile().name)
        } catch (e: RulesParserException) {
            Logger.log("Failed to initialize predicate tester for music pack $packPath! " +
                    "No music will be played. Error:\n${e.message}", LogLevel.ERROR)
        }
    }

    fun tick() {
        if (toStop != null) {
            client.soundManager.stop(toStop)
        }

        processFades()

        val nextMusic: PlayableSound? = getNextMusic()
        if (!shouldPlay(nextMusic))
        {
            return
        }

        currentMusicPredId = nextMusic?.getPredicateIdentifier() ?: ""
        startNewMusic(nextMusic)
    }

    private fun processFades() {
        fadeInstances.forEach { fadeInstance ->
            val volume: Float = fadeInstance.tick()
            setInstanceVolume(fadeInstance.soundInstance, musicVolumeOption.value.toFloat() * volume)
        }

        fadeInstances.removeIf { fadeinstance -> fadeinstance.done() }
    }

    private fun shouldPlay(music: PlayableSound?): Boolean {
        return (music == null ||
                music.getPredicateIdentifier() != currentMusicPredId || !isPlaying(soundInstance)) &&
                musicVolumeOption.value > 0
    }

    private fun startNewMusic(newMusic: PlayableSound?) {
        if (newMusic == null)
        {
            if (client.soundManager.isPlaying(soundInstance)) {
                fadeInstances.add(FadeInstance(soundInstance!!, false))
            }

            return
        }

        if (soundInstance == null) {
            soundInstance = newMusic.makeSoundInstance()
            client.soundManager.play(soundInstance)
            return
        }

        beginCrossfade(newMusic.makeSoundInstance())
    }

    private fun getNextMusic(): PlayableSound? {
        return (predicateTester?.getMusicToPlay(client) ?: listOf(null)).random()
    }

    private fun stop() {
        if (soundInstance != null)
        {
            client.soundManager.stop(soundInstance)
            client.soundManager.stop(oldSoundInstance)
            fadeInstances.forEach { fadeInstance -> client.soundManager.stop(fadeInstance.soundInstance) }
            fadeInstances.clear()
            soundInstance = null
            oldSoundInstance = null
        }
    }

    private fun beginCrossfade(newSoundInstance: SoundInstance) {
        oldSoundInstance = soundInstance
        soundInstance = newSoundInstance
        client.soundManager.play(soundInstance)

        fadeInstances.add(FadeInstance(soundInstance!!, true))
        fadeInstances.add(FadeInstance(oldSoundInstance!!, false))
    }

    private fun isPlaying(soundInstance: SoundInstance?): Boolean {
        return client.soundManager.isPlaying(soundInstance) && !(client.soundManager.soundSystem.sources[soundInstance]?.isStopped ?: true)
    }

    private fun setInstanceVolume(soundInstance: SoundInstance, volume: Float) {
        client.soundManager.soundSystem.sources[soundInstance]?.run { source ->
            source.isPlaying
            source.setVolume(volume)
        }
    }
}