package liltojustice.trueadaptivemusic.client.integration

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import liltojustice.trueadaptivemusic.client.gui.screen.MainScreen

class TrueAdaptiveMusicModMenu: ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { parent ->
            MainScreen(parent)
        }
    }
}