package com.azurewrath.quickstack.client.events;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import com.azurewrath.quickstack.QuickStack;
import com.azurewrath.quickstack.client.ClientUtils;

@EventBusSubscriber(modid = QuickStack.MOD_ID, value = Dist.CLIENT)
public class HotkeysEventHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (HotkeysRegistrar.DUMP_MAPPING.consumeClick()) {
            ClientUtils.sendNoSpectator(true);
        }
        while (HotkeysRegistrar.DEPOSIT_MAPPING.consumeClick()) {
            ClientUtils.sendNoSpectator(false);
        }
    }
}