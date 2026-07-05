package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.TAMClient
import net.minecraft.text.Text

fun TAMClient.allowedFileTypesText(): Text {
    return Text.literal(
        "${
            Text
                .translatableWithFallback("trueadaptivemusic.allowed_file_types", "Allowed file types")
                .string
        }: ${allowedFileTypes.joinToString(", ")}"
    )
}