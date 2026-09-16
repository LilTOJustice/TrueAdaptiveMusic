package liltojustice.trueadaptivemusic.client

import net.minecraft.client.gui.screens.Screen
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder

object TAMClientCache {
    val screenClasses = run {
        val reflections = Reflections(ConfigurationBuilder().addClassLoaders(ClassLoader.getSystemClassLoader()).setScanners(Scanners.SubTypes))
        (reflections
            .getSubTypesOf(Screen::class.java).takeIf { it.isNotEmpty() }
            ?: reflections.getSubTypesOf(Any::class.java))
            .mapNotNull { it.kotlin.qualifiedName }
            .toList()
    }

    fun init() {
    }
}