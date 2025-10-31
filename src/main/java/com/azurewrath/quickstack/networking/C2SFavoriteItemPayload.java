package com.azurewrath.quickstack.networking;

import com.azurewrath.quickstack.QuickStack;
import com.azurewrath.quickstack.util.ItemStackUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record C2SFavoriteItemPayload(int slotId) implements CustomPacketPayload {
    public static final Type<C2SFavoriteItemPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(QuickStack.MOD_ID, "favorite_item"));

    public static final StreamCodec<FriendlyByteBuf, C2SFavoriteItemPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeInt(payload.slotId),
            buf -> new C2SFavoriteItemPayload(buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SFavoriteItemPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Slot slot = player.containerMenu.getSlot(payload.slotId);
                ItemStack stack = slot.getItem();

                CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                CompoundTag stackTag = customData != null ? customData.copyTag() : null;

                if (stackTag != null && ItemStackUtils.isFavorited(stack)) {
                    // 移除收藏标记
                    stackTag.remove("favorite");
                    if (stackTag.isEmpty()) {
                        stack.remove(DataComponents.CUSTOM_DATA);
                    } else {
                        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(stackTag));
                    }
                } else {
                    // 添加收藏标记
                    CompoundTag newTag = stackTag != null ? stackTag.copy() : new CompoundTag();
                    newTag.putBoolean("favorite", true);
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(newTag));
                }
            }
        });
    }
}