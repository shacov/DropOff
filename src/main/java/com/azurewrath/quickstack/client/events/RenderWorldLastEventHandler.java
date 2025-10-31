package com.azurewrath.quickstack.client.events;

import com.azurewrath.quickstack.client.RendererCubeTarget;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import com.azurewrath.quickstack.QuickStack;
import com.azurewrath.quickstack.client.ClientUtils;
import com.azurewrath.quickstack.config.QuickStackConfig;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = QuickStack.MOD_ID, value = Dist.CLIENT)
public class RenderWorldLastEventHandler {

    @SubscribeEvent
    public static void onRenderWorldLastEvent(RenderLevelStageEvent event) {
        RendererCube.INSTANCE.tryToRender(event);
    }

    public static class RendererCube {

        public static final RendererCube INSTANCE = new RendererCube();
        private List<RendererCubeTarget> rendererCubeTargets = new ArrayList<>();
        private long lastDrawTime;

        public void draw(List<RendererCubeTarget> rendererCubeTargets) {
            this.rendererCubeTargets = rendererCubeTargets;
            lastDrawTime = System.currentTimeMillis();
        }

        /**
         * This method called by RenderWorldLastEvent handler.
         * It does nothing until the draw() method assign the necessary delay to the
         * global field named currentTime.
         */
        void tryToRender(RenderLevelStageEvent event) {
            long timeWhenDisappear = lastDrawTime + QuickStackConfig.CLIENT.highlightDelay.get();

            // 检查是否应该渲染（在延迟时间内且延迟不为负）
            boolean shouldRender = QuickStackConfig.CLIENT.highlightDelay.get() < 0 ||
                    System.currentTimeMillis() < timeWhenDisappear;

            if (shouldRender && !rendererCubeTargets.isEmpty()) {
                ClientUtils.renderBlocks(event, rendererCubeTargets);
            }
        }
    }
}