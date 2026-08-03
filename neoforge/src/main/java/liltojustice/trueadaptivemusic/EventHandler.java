package liltojustice.trueadaptivemusic;

import liltojustice.trueadaptivemusic.client.network.NeoforgeClientNetworkingInterface;
import liltojustice.trueadaptivemusic.network.TAMServerNetworking;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber
public class EventHandler implements IModBusEvent {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        TrueAdaptiveMusic.INSTANCE.getPhysicalServerServerNetworkingInterface().registerPayloadHandlers(event);
        TrueAdaptiveMusic.INSTANCE.getPhysicalClientServerNetworkingInterface().registerPayloadHandlers(event);
        NeoforgeClientNetworkingInterface.INSTANCE.registerPayloadHandlers(event);
    }

    @SubscribeEvent
    public static void onEndServerTick(ServerTickEvent.Post event) {
        TAMServerNetworking.INSTANCE.processTick(event.getServer());
    }
}
