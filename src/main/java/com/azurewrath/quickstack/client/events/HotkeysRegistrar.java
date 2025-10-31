package com.azurewrath.quickstack.client.events;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;
import com.azurewrath.quickstack.QuickStack;

@EventBusSubscriber(modid = QuickStack.MOD_ID, value = Dist.CLIENT)
public class HotkeysRegistrar {
    public static final KeyMapping DUMP_MAPPING = new KeyMapping("quickstack.key.dump", GLFW.GLFW_KEY_X, QuickStack.MOD_ID);
    public static final KeyMapping DEPOSIT_MAPPING = new KeyMapping("quickstack.key.deposit", GLFW.GLFW_KEY_C, QuickStack.MOD_ID);

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(DUMP_MAPPING);
        event.register(DEPOSIT_MAPPING);
    }
}