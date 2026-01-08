package com.azurewrath.quickstack.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.PacketDistributor;
import com.azurewrath.quickstack.QuickStack;
import com.azurewrath.quickstack.client.RendererCubeTarget;
import com.azurewrath.quickstack.config.QuickStackConfig;
import com.azurewrath.quickstack.util.ItemStackUtils;
import com.azurewrath.quickstack.util.SuccedableInventoryData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record C2SRequestQuickstackPayload(boolean ignoreHotbar, boolean dump,
                                          List<BlockEntityType<?>> teTypes, int minSlotCount) implements CustomPacketPayload {

    public static final Type<C2SRequestQuickstackPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(QuickStack.MOD_ID, "request_quickstack"));

    public static final StreamCodec<FriendlyByteBuf, C2SRequestQuickstackPayload> STREAM_CODEC = StreamCodec.of(
            C2SRequestQuickstackPayload::write,
            C2SRequestQuickstackPayload::new
    );

    public C2SRequestQuickstackPayload(FriendlyByteBuf buf) {
        this(
                buf.readBoolean(),
                buf.readBoolean(),
                readRegistryIdArray(buf),
                buf.readInt()
        );
    }

    public C2SRequestQuickstackPayload(boolean ignoreHotbar, boolean dump, List<BlockEntityType<?>> teTypes, int minSlotCount) {
        this.ignoreHotbar = ignoreHotbar;
        this.dump = dump;
        this.teTypes = teTypes != null ? teTypes : new ArrayList<>();
        this.minSlotCount = minSlotCount;
    }

    public static void write(FriendlyByteBuf buf, C2SRequestQuickstackPayload payload) {
        buf.writeBoolean(payload.ignoreHotbar);
        buf.writeBoolean(payload.dump);
        writeRegistryIdArray(buf, payload.teTypes);
        buf.writeInt(payload.minSlotCount);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SRequestQuickstackPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Set<SuccedableInventoryData> nearbyInventories = payload.getNearbyInventories(player);
                int itemsCounter = 0;

                for (SuccedableInventoryData inventoryData : nearbyInventories) {
                    BlockEntity blockEntity = inventoryData.blockEntity;
                    // 使用新的 Capabilities 系统获取 IItemHandler
                    IItemHandler itemHandler = player.level().getCapability(Capabilities.ItemHandler.BLOCK,
                            blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity, null);

                    if (itemHandler != null) {
                        if (payload.dump) {
                            itemsCounter += payload.quickStack(player, itemHandler, inventoryData);
                        } else {
                            itemsCounter += payload.quickStackExisting(player, itemHandler, inventoryData);
                        }
                    }
                }

                List<RendererCubeTarget> rendererCubeTargets = new ArrayList<>();
                int affectedContainers = 0;
                player.containerMenu.broadcastChanges();

                for (SuccedableInventoryData inventoryData : nearbyInventories) {
                    int color = inventoryData.success ? 0x00FF00 : 0xFF0000;
                    RendererCubeTarget rendererCubeTarget = new RendererCubeTarget(
                            inventoryData.blockEntity.getBlockPos(), color);
                    rendererCubeTargets.add(rendererCubeTarget);
                    if (inventoryData.success) {
                        affectedContainers++;
                    }
                }

                PacketDistributor.sendToPlayer(player, new S2CReportPayload(
                        itemsCounter, affectedContainers, nearbyInventories.size(), rendererCubeTargets));
            }
        });
    }

    private int quickStack(Player player, IItemHandler target, SuccedableInventoryData data) {
        IItemHandler playerstacks = new InvWrapper(player.getInventory());
        int counter = 0;
        for (int i = 0; i < 36; ++i) {
            if (ignoreHotbar && i < 9) continue;
            ItemStack playerstack = playerstacks.getStackInSlot(i);

            if (playerstack.isEmpty() || ItemStackUtils.isFavorited(playerstack)) continue;

            data.setSuccessful();
            counter += playerstack.getCount();
            ItemStack rem = playerstacks.extractItem(i, Integer.MAX_VALUE, false);

            for (int j = 0; j < target.getSlots(); ++j) {
                rem = target.insertItem(j, rem, false);
                if (rem.isEmpty()) break;
            }

            if (!rem.isEmpty()) {
                counter -= rem.getCount();
                playerstacks.insertItem(i, rem, false);
            }
        }
        return counter;
    }

    private int quickStackExisting(Player player, IItemHandler target, SuccedableInventoryData data) {
        IItemHandler playerstacks = new InvWrapper(player.getInventory());
        int counter = 0;
        for (int i = 0; i < 36; ++i) {
            if (ignoreHotbar && i < 9) continue;
            ItemStack playerstack = playerstacks.getStackInSlot(i);
            if (playerstack.isEmpty() || ItemStackUtils.isFavorited(playerstack)) continue;

            boolean hasExistingStack = IntStream.range(0, target.getSlots())
                    .mapToObj(target::getStackInSlot)
                    .filter(existing -> !existing.isEmpty())
                    .anyMatch(existing -> existing.getItem() == playerstack.getItem());
            if (!hasExistingStack) continue;

            data.setSuccessful();
            counter += playerstack.getCount();
            ItemStack rem = playerstacks.extractItem(i, Integer.MAX_VALUE, false);

            int[] emptySlots = new int[target.getSlots()];
            int numEmptySlots = 0;
            for (int j = 0; j < target.getSlots(); ++j) {
                if (target.getStackInSlot(j).isEmpty()) {
                    emptySlots[numEmptySlots] = j;
                    numEmptySlots++;
                }
                if (rem.getItem() != target.getStackInSlot(j).getItem()) continue;
                rem = target.insertItem(j, rem, false);
                if (rem.isEmpty()) break;
            }

            for (int j = 0; j < numEmptySlots; ++j) {
                rem = target.insertItem(emptySlots[j], rem, false);
                if (rem.isEmpty()) break;
            }

            if (!rem.isEmpty()) {
                counter -= rem.getCount();
                playerstacks.insertItem(i, rem, false);
            }
        }
        return counter;
    }

    private Set<SuccedableInventoryData> getNearbyInventories(ServerPlayer player) {
        double playerX = player.position().x;
        double playerY = player.position().y;
        double playerZ = player.position().z;
        int minX = (int) (playerX - QuickStackConfig.SERVER.scanRadius.get());
        int maxX = (int) (playerX + QuickStackConfig.SERVER.scanRadius.get());
        int minY = (int) (playerY - QuickStackConfig.SERVER.scanRadius.get());
        int maxY = (int) (playerY + QuickStackConfig.SERVER.scanRadius.get());
        int minZ = (int) (playerZ - QuickStackConfig.SERVER.scanRadius.get());
        int maxZ = (int) (playerZ + QuickStackConfig.SERVER.scanRadius.get());

        Level world = player.level();
        return BlockPos.betweenClosedStream(minX, minY, minZ, maxX, maxY, maxZ)
                .map(world::getBlockEntity)
                .filter(Objects::nonNull)
                .filter(tileEntity -> {
                    // 使用新的 Capabilities 系统检查是否有物品处理能力
                    IItemHandler handler = world.getCapability(Capabilities.ItemHandler.BLOCK,
                            tileEntity.getBlockPos(), tileEntity.getBlockState(), tileEntity, null);
                    return handler != null && handler.getSlots() >= minSlotCount;
                })
                .filter(tileEntity -> !teTypes.contains(tileEntity.getType()))
                .map(SuccedableInventoryData::new)
                .collect(Collectors.toSet());
    }

    private static void writeRegistryIdArray(FriendlyByteBuf buf, List<BlockEntityType<?>> entries) {
        buf.writeInt(entries.size());
        entries.forEach(type -> {
            // 使用新的方式写入注册表ID
            ResourceLocation key = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type);
            buf.writeResourceLocation(key);
        });
    }

    private static List<BlockEntityType<?>> readRegistryIdArray(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<BlockEntityType<?>> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ResourceLocation key = buf.readResourceLocation();
            BlockEntityType<?> type = BuiltInRegistries.BLOCK_ENTITY_TYPE.get(key);
            if (type != null) {
                list.add(type);
            }
        }
        return list;
    }
}
