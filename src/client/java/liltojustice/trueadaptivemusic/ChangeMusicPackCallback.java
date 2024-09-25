package liltojustice.trueadaptivemusic;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;

import java.nio.file.Path;

public interface ChangeMusicPackCallback {
    Event<ChangeMusicPackCallback> EVENT = EventFactory.createArrayBacked(ChangeMusicPackCallback.class,
            (listeners) -> (packPath) -> {
                for (ChangeMusicPackCallback listener : listeners) {
                    ActionResult result = listener.loadPack(packPath);

                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            });

    ActionResult loadPack(Path packPath);
}