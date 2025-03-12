package liltojustice.trueadaptivemusic.client;

import liltojustice.trueadaptivemusic.client.sound.PlayableSound;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.Nullable;

public interface PlaySoundNowCallback {
    Event<PlaySoundNowCallback> EVENT = EventFactory.createArrayBacked(PlaySoundNowCallback.class,
            (listeners) -> (sound) -> {
                for (PlaySoundNowCallback listener : listeners) {
                    ActionResult result = listener.playSoundNow(sound);

                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            });

    ActionResult playSoundNow(@Nullable PlayableSound sound);
}