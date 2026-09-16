package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.Logger
import net.minecraft.client.gui.screens.Screen
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

    fun init() {
    }
}