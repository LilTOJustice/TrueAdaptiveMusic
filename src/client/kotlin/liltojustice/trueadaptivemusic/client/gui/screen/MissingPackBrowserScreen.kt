package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Constants
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.ConfirmScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.util.Util

class MissingPackBrowserScreen(parent: Screen): ConfirmScreen(
    { confirmed ->
        if (confirmed) {
            Util.getPlatform().openUri(Constants.MODRINTH_PACK_BROWSER_URL)
        }

        Minecraft.getInstance().setScreen(parent)
    },
    Component.literal("Music Pack Browser mod missing"),
    Component.literal(
        "The pack browser mod addon is missing.\nIf you got True Adaptive Music from Curseforge, it doesn't " +
                "come preinstalled.\nWould you like to download it from Modrinth?"
    )
)