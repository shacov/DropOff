package com.azurewrath.quickstack.client;

import net.minecraft.core.BlockPos;

import java.util.Objects;

public class RendererCubeTarget {

    private final BlockPos blockPos;
    private final int color;

    public RendererCubeTarget(BlockPos blockPos, int color) {
        this.blockPos = blockPos;
        this.color = color;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public int getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "BlockPos: [" + blockPos.toString() + "] " +
                "Color: [" + color + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RendererCubeTarget that)) return false;
        return color == that.color && Objects.equals(blockPos, that.blockPos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockPos, color);
    }
}