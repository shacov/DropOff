package com.azurewrath.quickstack.client.events;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import com.azurewrath.quickstack.QuickStack;
import com.azurewrath.quickstack.client.ClientUtils;
import com.azurewrath.quickstack.config.QuickStackConfig;
import com.azurewrath.quickstack.networking.C2SFavoriteItemPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import com.azurewrath.quickstack.util.ItemStackUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = QuickStack.MOD_ID, value = Dist.CLIENT)
public class GuiEventHandler {

    @SubscribeEvent
    public static void onGuiOpen(ScreenEvent.Init.Post event) {
        if (!canDisplay(event.getScreen()) || !QuickStackConfig.CLIENT.showInventoryButton.get()) {
            return;
        }

        AbstractContainerScreen<?> containerScreen = (AbstractContainerScreen<?>) event.getScreen();

        boolean isCreative = Minecraft.getInstance().player.getAbilities().instabuild;

        int xPos = containerScreen.getGuiLeft() + 80 +
                (isCreative ? QuickStackConfig.CLIENT.creativeInventoryButtonXOffset.get()
                        : QuickStackConfig.CLIENT.survivalInventoryButtonXOffset.get());
        int yPos = containerScreen.getGuiTop() + 80
                + (isCreative ? QuickStackConfig.CLIENT.creativeInventoryButtonYOffset.get()
                : QuickStackConfig.CLIENT.survivalInventoryButtonYOffset.get());

        if (QuickStackConfig.CLIENT.enableDump.get()) {
            Button dump = Button.builder(Component.literal("^"), b -> actionPerformed(true))
                    .pos(xPos, yPos).size(10,14).tooltip(Tooltip.create(Component.translatable("quickstack.dump_nearby"))).build();
            event.addListener(dump);
        }

        Button deposit = Button.builder(Component.literal("^"), b -> actionPerformed(false))
                .pos(xPos + 12, yPos).size(10,14).tooltip(Tooltip.create(Component.translatable("quickstack.quick_stack"))).build();
        event.addListener(deposit);
    }

    private static void actionPerformed(boolean dump) {
        ClientUtils.sendNoSpectator(dump);
    }

    @SubscribeEvent
    public static void onItemClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!canDisplay(event.getScreen()) || !(event.getScreen() instanceof InventoryScreen containerScreen)
                || !Screen.hasControlDown())
            return;

        Slot slotUnderMouse = containerScreen.getSlotUnderMouse();
        if (slotUnderMouse != null && slotUnderMouse.hasItem()) {
            event.setCanceled(true);
            PacketDistributor.sendToServer(new C2SFavoriteItemPayload(containerScreen.getSlotUnderMouse().index));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    @SuppressWarnings("unchecked")
    public static void drawFavorites(ContainerScreenEvent.Render.Background event) {
        AbstractContainerScreen<AbstractContainerMenu> containerScreen = (AbstractContainerScreen<AbstractContainerMenu>) event.getContainerScreen();
        if (!canDisplay(containerScreen))
            return;
        AbstractContainerMenu playerContainer = containerScreen.getMenu();
        GuiGraphics matrices = event.getGuiGraphics();

        for (int k = 0; k < 3; ++k) {
            for (int j = 0; j < 9; ++j) {
                Slot slot = playerContainer.slots.get(j + (k + 1) * 9);
                ItemStack stack = slot.getItem();
                if (ItemStackUtils.isFavorited(stack)) {
                    int xoffset = 8;
                    int yoffset = 84;
                    matrices.fill(containerScreen.getGuiLeft() + j * 18 + xoffset,
                            containerScreen.getGuiTop() + k * 18 + yoffset,
                            containerScreen.getGuiLeft() + j * 18 + 16 + xoffset,
                            containerScreen.getGuiTop() + k * 18 + 16 + yoffset,
                            QuickStackConfig.favorite_color_cache << 8 | 0x80); // 添加透明度
                }
            }
        }
        List<ItemStack> stacks = playerContainer.getItems();

        for (int i = 0; i < 9; ++i) {
            ItemStack stack = stacks.get(i + 36);
            if (ItemStackUtils.isFavorited(stack)) {
                int xoffset = 8;
                int yoffset = 142;
                matrices.fill(containerScreen.getGuiLeft() + i * 18 + xoffset,
                        containerScreen.getGuiTop() + yoffset,
                        containerScreen.getGuiLeft() + i * 18 + 16 + xoffset,
                        containerScreen.getGuiTop() + 16 + yoffset,
                        QuickStackConfig.favorite_color_cache << 8 | 0x80); // 添加透明度
            }
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    public static boolean canDisplay(Screen screen) {
        return screen instanceof AbstractContainerScreen && canDisplay((AbstractContainerScreen<?>) screen);
    }

    private static final Set<Class<?>> bad_classes = new HashSet<>();

    public static <T extends AbstractContainerMenu> boolean canDisplay(AbstractContainerScreen<T> screen) {
        // 对于创造模式库存界面，只在显示玩家物品栏时返回true
        if (screen instanceof CreativeModeInventoryScreen creativeScreen) {
            // 检查是否在显示玩家物品栏（而不是创造模式标签页）
            // 在CreativeModeInventoryScreen中，当显示玩家物品栏时，getSelectedTab()会返回特定的标签
            // 我们通过检查是否不在创造模式标签页来判断是否在玩家物品栏页
            return creativeScreen.isInventoryOpen();
        }

        if (screen instanceof InventoryScreen)
            return true;
        if (screen instanceof HorseInventoryScreen)
            return false;
        try {
            MenuType<?> menuType = screen.getMenu().getType();
            var screenMenuRegistry = BuiltInRegistries.MENU.getKey(menuType).toString();
            return QuickStackConfig.CLIENT.whitelistedContainers.get().contains(screenMenuRegistry);
        } catch (Exception e) {
            Class<?> clazz = screen.getMenu().getClass();
            if (!bad_classes.contains(clazz)) {
                QuickStack.LOGGER.error(clazz + " does not have a container type registered to it! " +
                        "This is a bug in the other mod and should be reported to them.  The buttons will not display in this gui!");
                bad_classes.add(clazz);
            }
            return false;
        }
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent e) {
        ItemStack stack = e.getItemStack();
        if (ItemStackUtils.isFavorited(stack)) {
            e.getToolTip().add(Component.translatable("quickstack.tooltip.favorited"));
        }
    }
}