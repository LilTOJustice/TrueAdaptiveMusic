package liltojustice.trueadaptivemusic.client

import net.minecraft.client.gui.screens.Screen
import org.reflections.Reflections
import org.reflections.scanners.Scanners

object TAMClientCache {
    val screenClasses = findScreenClasses()

    fun init() {
    }

    private fun findScreenClasses(): List<String> {
        return Reflections(Scanners.SubTypes)
            .getSubTypesOf(Screen::class.java)
            .mapNotNull { it.kotlin.qualifiedName }
    }
}