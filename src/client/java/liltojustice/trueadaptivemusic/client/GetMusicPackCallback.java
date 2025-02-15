package liltojustice.trueadaptivemusic.client;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.Nullable;

public interface GetMusicPackCallback {
    Event<GetMusicPackCallback> EVENT = EventFactory.createArrayBacked(GetMusicPackCallback.class,
            (listeners) -> (packResult) -> {
                for (GetMusicPackCallback listener : listeners) {
                    ActionResult result = listener.getPack(packResult);

                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            });

    ActionResult getPack(@Nullable MusicPack[] packResult);
}