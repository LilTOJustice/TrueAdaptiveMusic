package liltojustice.trueadaptivemusic;

import liltojustice.trueadaptivemusic.network.TAMServerNetworking;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.IModBusEvent;

@Mod.EventBusSubscriber
public class EventHandler implements IModBusEvent {
    @SubscribeEvent
    public static void onEndServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            TAMServerNetworking.INSTANCE.processTick(event.getServer());
        }
    }
}
