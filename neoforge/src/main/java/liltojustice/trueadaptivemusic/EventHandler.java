package liltojustice.trueadaptivemusic;

import liltojustice.trueadaptivemusic.client.network.NeoforgeClientNetworkingInterface;
import liltojustice.trueadaptivemusic.network.TAMServerNetworking;
import liltojustice.trueadaptivemusic.network.NeoforgeServerNetworkingInterface;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = "trueadaptivemusic")
public class EventHandler {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        NeoforgeServerNetworkingInterface.INSTANCE.registerPayloadHandlers(event);
        NeoforgeClientNetworkingInterface.INSTANCE.registerCommonPayloadHandlers(event);
    }

    @SubscribeEvent
    public static void register(RegisterClientPayloadHandlersEvent event) {
        NeoforgeClientNetworkingInterface.INSTANCE.registerPayloadHandlers(event);
    }

    @SubscribeEvent
    public static void onEndServerTick(ServerTickEvent.Post event) {
        TAMServerNetworking.INSTANCE.processTick(event.getServer());
    }
}
