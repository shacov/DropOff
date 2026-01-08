package com.azurewrath.quickstack.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import com.azurewrath.quickstack.QuickStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuickStackConfig {

    public static final Client CLIENT;
    public static final ModConfigSpec CLIENT_SPEC;

    public static final Server SERVER;
    public static final ModConfigSpec SERVER_SPEC;

    static {
        final var clientSpecPair = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = clientSpecPair.getRight();
        CLIENT = clientSpecPair.getLeft();

        final var serverSpecPair = new ModConfigSpec.Builder().configure(Server::new);
        SERVER_SPEC = serverSpecPair.getRight();
        SERVER = serverSpecPair.getLeft();
    }

    public static class Server {
        public final ModConfigSpec.IntValue scanRadius;

        public Server(ModConfigSpec.Builder builder) {
            builder.push("general");
            scanRadius = builder.comment("Radius in blocks to check containers around the player.")
                    .defineInRange("Scan radius", 6, 0, Integer.MAX_VALUE);
            builder.pop();
        }
    }

    public static class Client {
        public final ModConfigSpec.BooleanValue highlightContainers;
        public final ModConfigSpec.BooleanValue showInventoryButton;
        public final ModConfigSpec.BooleanValue displayMessage;
        public final ModConfigSpec.BooleanValue ignoreHotBar;
        public final ModConfigSpec.BooleanValue enableDump;

        public final ModConfigSpec.IntValue creativeInventoryButtonXOffset;
        public final ModConfigSpec.IntValue creativeInventoryButtonYOffset;
        public final ModConfigSpec.IntValue minSlotCount;
        public final ModConfigSpec.IntValue survivalInventoryButtonXOffset;
        public final ModConfigSpec.IntValue survivalInventoryButtonYOffset;
        public final ModConfigSpec.IntValue highlightDelay;

        public final ModConfigSpec.ConfigValue<String> favorite_color;
        public final ModConfigSpec.ConfigValue<List<String>> blacklistedTes;
        public final ModConfigSpec.ConfigValue<List<String>> whitelistedContainers;

        public Client(ModConfigSpec.Builder builder) {
            builder.push("general");

            // booleans
            enableDump = builder.comment("Enable dump button.").define("Enable Dump Button", true);
            ignoreHotBar = builder.comment("Ignore hotbar when transferring.").define("Ignore Hotbar", true);
            highlightContainers = builder.comment("Highlight nearby containers.").define("Highlight containers", true);
            displayMessage = builder.comment(" information to the chat when task is complete.")
                    .define("Display Message", true);
            showInventoryButton = builder.comment("Show button in the player inventory.")
                    .define("Show inventory button", true);

            // Integers
            creativeInventoryButtonXOffset = builder.comment("Creative inventory button position width offset.")
                    .defineInRange("Creative inventory button X offset", 68, Integer.MIN_VALUE, Integer.MAX_VALUE);
            creativeInventoryButtonYOffset = builder.comment("Creative inventory button position height offset.")
                    .defineInRange("Creative inventory button Y offset", -78, Integer.MIN_VALUE, Integer.MAX_VALUE);
            highlightDelay = builder.comment("Blocks highlighting delay in milliseconds. Delay < 0 means forever.")
                    .defineInRange("Highlight delay", 3000, -1, Integer.MAX_VALUE);
            minSlotCount = builder.comment("Min number of slots that a container can be eligible for transfer to")
                    .defineInRange("Minimum Slots", 6, 0, Integer.MAX_VALUE);
            survivalInventoryButtonXOffset = builder.comment("Survival inventory button position width offset.")
                    .defineInRange("Survival inventory button X offset", 50, Integer.MIN_VALUE, Integer.MAX_VALUE);
            survivalInventoryButtonYOffset = builder.comment("Survival inventory button position height offset.")
                    .defineInRange("Survival inventory button Y offset", -18, Integer.MIN_VALUE, Integer.MAX_VALUE);

            // other
            favorite_color = builder.comment("favorites color background").define("favorite_color", "#FFFFBB");
            blacklistedTes = builder.define("Blacklisted Block Entities",
                    List.of("minecraft:furnace", "minecraft:blast_furnace", "minecraft:smoker"));
            whitelistedContainers = builder.define("Whitelisted containers",
                    List.of("curios:curios_container"));

            builder.pop();
        }
    }

    public static List<BlockEntityType<?>> blockEntityBlacklist = new ArrayList<>();
    public static int favorite_color_cache = 0xFFFFBB; // 默认颜色

    public static void onConfigChanged(ModConfigEvent event) {
        if (!event.getConfig().getModId().equals(QuickStack.MOD_ID)) {
            return;
        }

        // 只在客户端处理客户端配置
        if (event.getConfig().getType() == net.neoforged.fml.config.ModConfig.Type.CLIENT) {
            // 确保我们在客户端环境
            if (FMLEnvironment.dist == Dist.CLIENT) {
                try {
                    blockEntityBlacklist = CLIENT.blacklistedTes.get()
                            .stream()
                            .map(ResourceLocation::tryParse)
                            .filter(resourceLocation -> {
                                boolean exists = BuiltInRegistries.BLOCK_ENTITY_TYPE.containsKey(resourceLocation);
                                if (!exists) {
                                    QuickStack.LOGGER.warn("Ignoring unknown blockentity: " + resourceLocation);
                                }
                                return exists;
                            })
                            .map(BuiltInRegistries.BLOCK_ENTITY_TYPE::get)
                            .collect(Collectors.toList());

                    favorite_color_cache = Integer.decode(CLIENT.favorite_color.get().replace("#", "0x"));

                    QuickStack.LOGGER.info("Configuration changed.");
                } catch (IllegalStateException e) {
                    // 如果配置尚未加载，记录警告并跳过
                    QuickStack.LOGGER.warn("Client config not loaded yet, using default values.");
                }
            }
        }
    }
}
