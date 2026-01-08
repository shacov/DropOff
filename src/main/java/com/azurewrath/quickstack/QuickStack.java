package com.azurewrath.quickstack;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
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
        // 根据运行环境注册配置
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modContainer.registerConfig(ModConfig.Type.CLIENT, QuickStackConfig.CLIENT_SPEC);
        }
        modContainer.registerConfig(ModConfig.Type.SERVER, QuickStackConfig.SERVER_SPEC);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(QuickStackConfig::onConfigChanged);
        modEventBus.addListener(NetworkHandler::register);

        LOGGER.info("QuickStack mod initialized for " + FMLEnvironment.dist + " environment");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // 网络注册将在后续文件中处理
    }
}
