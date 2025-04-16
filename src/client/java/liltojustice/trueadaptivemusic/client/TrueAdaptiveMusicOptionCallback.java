package liltojustice.trueadaptivemusic.client;

import com.mojang.serialization.Codec;
import liltojustice.trueadaptivemusic.client.gui.screen.MainScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public record TrueAdaptiveMusicOptionCallback<T>(MinecraftClient client)
        implements SimpleOption.Callbacks<T> {
    @Override
    public Function<SimpleOption<T>, ClickableWidget> getWidgetCreator(
            SimpleOption.TooltipFactory<T> tooltipFactory,
            GameOptions gameOptions,
            int x,
            int y,
            int width,
            Consumer<T> changeCallback) {
        return option -> {
            assert client.currentScreen != null;
            return new ButtonWidget.Builder(Text.literal("True Adaptive Music"),
                    widget -> client.setScreen(new MainScreen(client.currentScreen)))
                    .dimensions(x, y, width, 20).build();
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
