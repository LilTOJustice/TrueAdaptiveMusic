package liltojustice.trueadaptivemusic.client.cache

import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.cache.ticker.types.LastNonRiverBiomeCacheTicker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.Holder
import net.minecraft.world.level.biome.Biome
import org.reflections.Reflections
import org.reflections.scanners.Scanners

object TAMClientCache {
    val screenClasses = run {
        try {
            Reflections(Scanners.SubTypes)
                .getSubTypesOf(Screen::class.java)
                .mapNotNull { it.kotlin.qualifiedName }
        }
        catch (t: Throwable) {
            Logger.logError(
                "Failed to get Screen subtypes for Screen predicate." +
                        "\nError: ${t.message}\n${t.stackTraceToString()}"
            )

            emptyList()
        }
    }

    val lastNonRiverBiome: Holder<Biome>?
        get() = lastNonRiverBiomeCacheTicker.getValue()

    private var initialized = false
    private var lastNonRiverBiomeCacheTicker = LastNonRiverBiomeCacheTicker()

    fun init() {
        if (initialized) {
            return
        }

        initialized = true
    }

    fun tick() {
        val minecraft = Minecraft.getInstance()
        lastNonRiverBiomeCacheTicker.tick(minecraft)
    }
}