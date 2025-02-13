package liltojustice.trueadaptivemusic.client;

import liltojustice.trueadaptivemusic.Constants;
import liltojustice.trueadaptivemusic.LogLevel;
import liltojustice.trueadaptivemusic.Logger;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

public interface ChangeMusicPackCallback {
    Event<ChangeMusicPackCallback> EVENT = EventFactory.createArrayBacked(ChangeMusicPackCallback.class,
            (listeners) -> (pack) -> {
                for (ChangeMusicPackCallback listener : listeners) {
                    ActionResult result = listener.selectPack(pack);
                    try (FileOutputStream outputStream = new FileOutputStream(
                            Paths.get(Constants.SELECTED_PACK).toFile(), false)) {
                        outputStream.write(pack.getPath().getFileName().toString().getBytes());
                    } catch (IOException ignored) {
                        Logger.Companion.log(
                                "Failed to save selected pack " + pack.getPath().getFileName(), LogLevel.ERROR);
                    }

                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            });

    ActionResult selectPack(MusicPack musicPack);
}