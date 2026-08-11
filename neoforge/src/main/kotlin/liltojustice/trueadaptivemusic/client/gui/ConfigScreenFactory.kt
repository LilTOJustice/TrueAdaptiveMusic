package liltojustice.trueadaptivemusic.client.gui

import liltojustice.trueadaptivemusic.client.gui.screen.MainScreen
import net.minecraft.client.gui.screens.Screen
import net.neoforged.fml.ModContainer
import net.neoforged.neoforge.client.gui.IConfigScreenFactory

object ConfigScreenFactory: IConfigScreenFactory {
    override fun createScreen(
        container: ModContainer,
        modListScreen: Screen
    ): Screen {
        return MainScreen(modListScreen)
    }
}