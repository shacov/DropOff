package com.azurewrath.quickstack.task;

import com.azurewrath.quickstack.client.ClientUtils;
import com.azurewrath.quickstack.client.RendererCubeTarget;
import com.azurewrath.quickstack.client.events.RenderWorldLastEventHandler;
import com.azurewrath.quickstack.config.QuickStackConfig;

import java.util.List;

import static com.azurewrath.quickstack.util.MessageUtils.red;

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
            String message = red(String.valueOf(itemsCounter)) +
                    " items moved to " + red(String.valueOf(affectedContainers)) +
                    " containers of " + red(String.valueOf(totalContainers)) +
                    " checked in total.";

            ClientUtils.printToChat(message);
        }
    }
}