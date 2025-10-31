package com.azurewrath.quickstack;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.azurewrath.quickstack.config.QuickStackConfig;
import com.azurewrath.quickstack.util.LogMessageFactory;
import com.azurewrath.quickstack.networking.NetworkHandler;

@Mod(QuickStack.MOD_ID)
public class QuickStack {

    public static final String MOD_ID = "quickstack";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID, LogMessageFactory.INSTANCE);

    public QuickStack(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, QuickStackConfig.CLIENT_SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, QuickStackConfig.SERVER_SPEC);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(QuickStackConfig::onConfigChanged);
        modEventBus.addListener(NetworkHandler::register);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // 网络注册将在后续文件中处理
    }
}