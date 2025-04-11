package liltojustice.trueadaptivemusic.client;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.Nullable;

public interface InvokeMusicEventCallback {
    Event<InvokeMusicEventCallback> EVENT = EventFactory.createArrayBacked(InvokeMusicEventCallback.class,
            (listeners) -> (eventType, args) -> {
                for (InvokeMusicEventCallback listener : listeners) {
                    ActionResult result = listener.invokeMusicEvent(eventType, args);

                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            });

    ActionResult invokeMusicEvent(String eventType, @Nullable Object... eventArgs);
}