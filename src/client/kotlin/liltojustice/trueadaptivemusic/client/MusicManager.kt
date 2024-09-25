package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicateTree
import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.sound.SoundInstance
import net.minecraft.registry.Registries
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists

class MusicManager(
    private val client: MinecraftClient) {
    private var currentSoundPack: String = ""
    private var predicateTester: MusicPredicateTree? = null
    private var soundInstance: SoundInstance? = null
    private var currentMusic: String = ""

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
        val musicPath: String = predicateTester?.getMusicToPlay(client) ?: ""
        if (!shouldPlay(musicPath))
        {
            return
        }

        currentMusic = musicPath

        stop()

        if (musicPath == "")
        {
            return
        }

        var asPath: Path? = null
        try
        {
            asPath = Path("${Constants.MUSIC_PACK_DIR}/$currentSoundPack/$musicPath")
        }
        catch (_: Exception) {}

        var asSoundEvent: SoundEvent? = null
        try
        {
            asSoundEvent = Registries.SOUND_EVENT.get(Identifier(musicPath))
        }
        catch (_: Exception) {}

        soundInstance = if (asSoundEvent != null) {
            PositionedSoundInstance.master(asSoundEvent, 1F)
        } else if (asPath != null && asPath.exists()) {
            AdaptiveMusicSoundInstance(asPath)
        } else {
            throw Exception("Path $musicPath is neither a valid SoundEvent nor sound file.")
        }

        client.soundManager.play(soundInstance)
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
}