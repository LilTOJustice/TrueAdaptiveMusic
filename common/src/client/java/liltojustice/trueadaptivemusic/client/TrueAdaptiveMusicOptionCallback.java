package liltojustice.trueadaptivemusic.client;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import liltojustice.trueadaptivemusic.Constants;
import liltojustice.trueadaptivemusic.client.gui.screen.MainScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public record TrueAdaptiveMusicOptionCallback(Minecraft minecraft) implements OptionInstance.ValueSet<Unit> {
    @Override
    public @NonNull Function<OptionInstance<Unit>, AbstractWidget> createButton(
            OptionInstance.@NonNull TooltipSupplier<Unit> tooltipFactory,
            @NonNull Options options,
            int x,
            int y,
            int width,
            @NonNull Consumer<Unit> changeCallback
    ) {
        return _ -> {
            assert minecraft.screen != null;
            return new Button.Builder(
                    Component.translatableWithFallback(
                            Constants.TRUE_ADAPTIVE_MUSIC + '.' + Constants.TRUE_ADAPTIVE_MUSIC,
                            "True Adaptive Music"
                    ),
                    _ -> minecraft.setScreen(new MainScreen(minecraft.screen))
            ).bounds(x, y, width, 20).build();
        };
    }

    @Override
    public @NonNull Optional<Unit> validateValue(@NonNull Unit value) {
        return Optional.of(value);
    }

    @Override
    @NotNull
    public Codec<Unit> codec() {
        return Codec.EMPTY.codec();
    }
}
