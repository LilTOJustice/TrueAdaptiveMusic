package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.TAMClient
import net.minecraft.network.chat.Component

fun TAMClient.allowedFileTypesText(): Component {
    return Component.literal(
        "${
            Component
                .translatableWithFallback("trueadaptivemusic.allowed_file_types", "Allowed file types")
                .string
        }: ${allowedFileTypes.joinToString(", ")}"
    )
}