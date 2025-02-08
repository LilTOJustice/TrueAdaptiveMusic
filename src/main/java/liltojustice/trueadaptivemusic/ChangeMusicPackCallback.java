package liltojustice.trueadaptivemusic;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public interface ChangeMusicPackCallback {
    Event<ChangeMusicPackCallback> EVENT = EventFactory.createArrayBacked(ChangeMusicPackCallback.class,
            (listeners) -> (packPath) -> {
                for (ChangeMusicPackCallback listener : listeners) {
                    ActionResult result = listener.loadPack(packPath);
                    try (FileOutputStream outputStream = new FileOutputStream(Paths.get(Constants.SELECTED_PACK).toFile(), false)) {
                        outputStream.write(packPath.getFileName().toString().getBytes());
                    } catch (IOException ignored) {
                        Logger.Companion.log("Failed to save selected pack " + packPath.toString(), LogLevel.ERROR);
                    }

                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            });

    ActionResult loadPack(Path packPath);
}