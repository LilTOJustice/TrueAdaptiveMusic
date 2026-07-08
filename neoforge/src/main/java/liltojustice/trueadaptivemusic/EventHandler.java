package liltojustice.trueadaptivemusic;

import liltojustice.trueadaptivemusic.common.network.TAMServerNetworking;
import liltojustice.trueadaptivemusic.network.NetworkingCommon;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = "trueadaptivemusic")
public class EventHandler {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        NetworkingCommon.INSTANCE.registerPayloadHandlers(event);
    }

    @SubscribeEvent
    public static void onEndServerTick(ServerTickEvent.Post event) {
        TAMServerNetworking.INSTANCE.processTick(event.getServer());
    }
}
