package liltojustice.trueadaptivemusic.client;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.Nullable;

public interface GetMusicManagerCallback {
    Event<GetMusicManagerCallback> EVENT = EventFactory.createArrayBacked(GetMusicManagerCallback.class,
            (listeners) -> (managerResult) -> {
                for (GetMusicManagerCallback listener : listeners) {
                    ActionResult result = listener.getMusicManager(managerResult);

                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            });

    ActionResult getMusicManager(@Nullable MusicManager[] managerResult);
}