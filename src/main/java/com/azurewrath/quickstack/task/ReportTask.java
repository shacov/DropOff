package com.azurewrath.quickstack.task;

import com.azurewrath.quickstack.client.ClientUtils;
import com.azurewrath.quickstack.client.RendererCubeTarget;
import com.azurewrath.quickstack.client.events.RenderWorldLastEventHandler;
import com.azurewrath.quickstack.config.QuickStackConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ReportTask implements Runnable {

    private final int itemsCounter;
    private final int affectedContainers;
    private final int totalContainers;
    private final List<RendererCubeTarget> rendererCubeTargets;

    public ReportTask(int itemsCounter, int affectedContainers, int totalContainers,
                      List<RendererCubeTarget> rendererCubeTargets) {
        this.itemsCounter = itemsCounter;
        this.affectedContainers = affectedContainers;
        this.totalContainers = totalContainers;
        this.rendererCubeTargets = rendererCubeTargets;
    }

    @Override
    public void run() {
        // 只有在启用高亮时才设置渲染目标
        if (QuickStackConfig.CLIENT.highlightContainers.get()) {
            RenderWorldLastEventHandler.RendererCube.INSTANCE.draw(rendererCubeTargets);
        }

        if (QuickStackConfig.CLIENT.displayMessage.get()) {
            // 正确构建 Component，而不是字符串
            Component message = Component.literal("")
                    .append(Component.literal(String.valueOf(itemsCounter)).withStyle(ChatFormatting.RED))
                    .append(" ")
                    .append(Component.translatable("quickstack.message.items_moved_to"))
                    .append(" ")
                    .append(Component.literal(String.valueOf(affectedContainers)).withStyle(ChatFormatting.RED))
                    .append(" ")
                    .append(Component.translatable("quickstack.message.containers_of"))
                    .append(" ")
                    .append(Component.literal(String.valueOf(totalContainers)).withStyle(ChatFormatting.RED))
                    .append(" ")
                    .append(Component.translatable("quickstack.message.checked_in_total"))
                    .append(".");

            ClientUtils.printToChat(message);
        }
    }
}
