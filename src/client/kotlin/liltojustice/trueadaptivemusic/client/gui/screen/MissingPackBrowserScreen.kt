package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Constants
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ConfirmScreen
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import net.minecraft.util.Util

class MissingPackBrowserScreen(parent: Screen): ConfirmScreen(
    { confirmed ->
        if (confirmed) {
            Util.getOperatingSystem().open(Constants.MODRINTH_PACK_BROWSER_URL)
        }

        MinecraftClient.getInstance().setScreen(parent)
    },
    Text.literal("Music Pack Browser mod missing"),
    Text.literal(
        "The pack browser mod addon is missing.\nIf you got True Adaptive Music from Curseforge, it doesn't " +
                "come preinstalled.\nWould you like to download it from Modrinth?"
    )
)