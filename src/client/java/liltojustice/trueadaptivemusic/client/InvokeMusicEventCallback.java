package liltojustice.trueadaptivemusic.client;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;

public interface InvokeMusicEventCallback {
    Event<InvokeMusicEventCallback> EVENT = EventFactory.createArrayBacked(InvokeMusicEventCallback.class,
            (listeners) -> (eventType) -> {
                for (InvokeMusicEventCallback listener : listeners) {
                    ActionResult result = listener.invokeMusicEvent(eventType);

                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            });

    ActionResult invokeMusicEvent(String eventType);
}