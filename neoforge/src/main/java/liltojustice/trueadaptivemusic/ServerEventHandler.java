package liltojustice.trueadaptivemusic;

import liltojustice.trueadaptivemusic.network.TAMServerNetworking;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class ServerEventHandler {
    @SubscribeEvent
    public static void onEndServerTick(ServerTickEvent.Post event) {
        TAMServerNetworking.INSTANCE.processTick(event.getServer());
    }
}
