package liltojustice.trueadaptivemusic.client;

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

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public record TrueAdaptiveMusicOptionCallback<T>(Minecraft minecraft) extends OptionInstance<T> {
    @Override
    public Function<OptionInstance<T>, AbstractWidget> getWidgetCreator(
            OptionInstance.TooltipSupplier<T> tooltipFactory,
            Options options,
            int x,
            int y,
            int width,
            Consumer<T> changeCallback
    ) {
        return _ -> {
            assert minecraft.screen != null;
            return new Button.Builder(
                    Component.translatableWithFallback(
                            "trueadaptivemusic.trueadaptivemusic",
                            "True Adaptive Music"
                    ),
                    widget -> minecraft.setScreen(new MainScreen(minecraft.screen))
            ).bounds(x, y, width, 20).build();
        };
    }

    @Override
    public Optional<T> validate(T value) {
        return Optional.of(value);
    }

    @Override
    public Codec<T> codec() {
        return null;
    }
}
