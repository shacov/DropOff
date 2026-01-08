package com.azurewrath.quickstack.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import com.azurewrath.quickstack.config.QuickStackConfig;
import com.azurewrath.quickstack.networking.C2SRequestQuickstackPayload;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class ClientUtils {

    public static void printToChat(String message) {
        message = "[" + ChatFormatting.BLUE + "QuickStack" + ChatFormatting.RESET + "]: " + message;

        LocalPlayer player = Minecraft.getInstance().player;
        var textComponentString = Component.literal(message);

        player.sendSystemMessage(textComponentString);
    }

    public static void sendNoSpectator(boolean dump) {
        if (Minecraft.getInstance().player.isSpectator()) {
            printToChat("Action not allowed in spectator mode.");
        } else {
            // 使用安全的配置访问方式
            C2SRequestQuickstackPayload quickstackMessage = new C2SRequestQuickstackPayload(
                    QuickStackConfig.CLIENT.ignoreHotBar.get(),
                    dump,
                    QuickStackConfig.blockEntityBlacklist,
                    QuickStackConfig.CLIENT.minSlotCount.get());
            PacketDistributor.sendToServer(quickstackMessage);
        }
    }

    public static void renderBlocks(RenderLevelStageEvent event, List<RendererCubeTarget> rendererCubeTargets) {
        // 确保在正确的渲染阶段
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
            return;
        }

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        PoseStack poseStack = event.getPoseStack();

        // 获取相机位置
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        poseStack.pushPose();
        // 应用相机偏移 - 这是关键修复
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        for (RendererCubeTarget target : rendererCubeTargets) {
            VertexConsumer builder = buffer.getBuffer(RenderType.lines());
            AABB boundingBox = Shapes.block().bounds().move(target.getBlockPos());

            // 提取颜色分量
            float red = ((target.getColor() >> 16) & 0xFF) / 255.0F;
            float green = ((target.getColor() >> 8) & 0xFF) / 255.0F;
            float blue = (target.getColor() & 0xFF) / 255.0F;
            float alpha = 1.0F; // 不透明度

            // 渲染线框
            LevelRenderer.renderLineBox(
                    poseStack,
                    builder,
                    boundingBox,
                    red, green, blue, alpha
            );
        }

        buffer.endBatch(RenderType.lines());
        poseStack.popPose();
    }
}
