package liltojustice.trueadaptivemusic;

import liltojustice.trueadaptivemusic.client.network.NeoforgeClientNetworkingInterface;
import liltojustice.trueadaptivemusic.network.NeoforgeServerNetworkingInterface;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class EventHandler implements IModBusEvent {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        NeoforgeServerNetworkingInterface.INSTANCE.registerPayloadHandlers(event);
    }

    @SubscribeEvent
    public static void register(RegisterClientPayloadHandlersEvent event) {
        NeoforgeClientNetworkingInterface.INSTANCE.registerPayloadHandlers(event);
    }
}
