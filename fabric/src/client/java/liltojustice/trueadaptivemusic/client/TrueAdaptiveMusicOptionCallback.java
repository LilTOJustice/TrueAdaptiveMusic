package liltojustice.trueadaptivemusic.client;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import liltojustice.trueadaptivemusic.client.gui.screen.MainScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public record TrueAdaptiveMusicOptionCallback(Minecraft minecraft) implements OptionInstance.ValueSet<Unit> {
    @Override
    public @NonNull Function<OptionInstance<Unit>, AbstractWidget> createButton(
            OptionInstance.@NonNull TooltipSupplier<Unit> tooltip,
            @NonNull Options options,
            int x,
            int y,
            int width,
            OptionInstance.@NonNull ValueUpdateListener<? super Unit> onValueChanged
    ) {
        return _ -> {
            assert minecraft.gui.screen() != null;
            return new Button.Builder(
                    Component.translatableWithFallback(
                            "trueadaptivemusic.trueadaptivemusic",
                            "True Adaptive Music"
                    ),
                    _ -> minecraft.gui.setScreen(new MainScreen(minecraft.gui.screen()))
            ).bounds(x, y, width, 20).build();
        };
    }

    @Override
    public @NonNull Optional<Unit> validateValue(@NonNull Unit value) {
        return Optional.of(value);
    }

    @Override
    @NotNull
    public Codec<com.mojang.datafixers.util.Unit> codec() {
        return Codec.EMPTY.codec();
    }
}
