package liltojustice.trueadaptivemusic.client;

import com.mojang.serialization.Codec;
import liltojustice.trueadaptivemusic.client.gui.screen.MainScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.Options;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public record TrueAdaptiveMusicOptionCallback<T>(Minecraft client)
        implements OptionInstance.ValueSet<T> {
    @Override
    public Function<OptionInstance<T>, AbstractWidget> createButton(
            OptionInstance.TooltipSupplier<T> tooltipFactory,
            Options gameOptions,
            int x,
            int y,
            int width,
            Consumer<T> changeCallback) {
        return option -> {
            assert client.screen != null;
            return new Button.Builder(
                    Component.translatableWithFallback(
                            "trueadaptivemusic.trueadaptivemusic",
                            "True Adaptive Music"
                    ),
                    widget -> client.setScreen(new MainScreen(client.screen))
            ).bounds(x, y, width, 20).build();
        };
    }

    @Override
    public Optional<T> validateValue(T value) {
        return Optional.of(value);
    }

    @Override
    public Codec<T> codec() {
        return null;
    }
}
