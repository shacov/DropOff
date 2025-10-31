package com.azurewrath.quickstack.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;

public class ItemStackUtils {
    public static boolean isFavorited(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return false;
        }
        CompoundTag stackTag = customData.copyTag();
        return stackTag.getBoolean("favorite");
    }
}