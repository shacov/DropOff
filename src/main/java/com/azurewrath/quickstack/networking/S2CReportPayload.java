package com.azurewrath.quickstack.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.azurewrath.quickstack.QuickStack;
import com.azurewrath.quickstack.client.RendererCubeTarget;
import com.azurewrath.quickstack.task.ReportTask;

import java.util.ArrayList;
import java.util.List;

public record S2CReportPayload(int itemsCounter, int affectedContainers,
                               int totalContainers, List<RendererCubeTarget> rendererCubeTargets) implements CustomPacketPayload {

    public static final Type<S2CReportPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(QuickStack.MOD_ID, "report"));

    // 修复 StreamCodec - 使用正确的参数顺序
    public static final StreamCodec<FriendlyByteBuf, S2CReportPayload> STREAM_CODEC = StreamCodec.of(
            S2CReportPayload::write,  // 编码器
            S2CReportPayload::new     // 解码器
    );

    public S2CReportPayload(FriendlyByteBuf buf) {
        this(
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                readRendererCubeTargets(buf)
        );
    }

    // 将 encode 方法重命名为 write 以匹配 StreamCodec 的约定
    public static void write(FriendlyByteBuf buf, S2CReportPayload payload) {
        buf.writeInt(payload.itemsCounter);
        buf.writeInt(payload.affectedContainers);
        buf.writeInt(payload.totalContainers);
        writeRendererCubeTargets(buf, payload.rendererCubeTargets);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CReportPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ReportTask reportTask = new ReportTask(
                    payload.itemsCounter,
                    payload.affectedContainers,
                    payload.totalContainers,
                    payload.rendererCubeTargets
            );
            reportTask.run();
        });
    }

    private static void writeRendererCubeTargets(FriendlyByteBuf buf, List<RendererCubeTarget> rendererCubeTargets) {
        buf.writeInt(rendererCubeTargets.size());
        rendererCubeTargets.forEach(target -> {
            buf.writeLong(target.getBlockPos().asLong());
            buf.writeInt(target.getColor());
        });
    }

    private static List<RendererCubeTarget> readRendererCubeTargets(FriendlyByteBuf buf) {
        int targetsLen = buf.readInt();
        List<RendererCubeTarget> list = new ArrayList<>();
        for (int i = 0; i < targetsLen; i++) {
            BlockPos blockPos = BlockPos.of(buf.readLong());
            int color = buf.readInt();
            list.add(new RendererCubeTarget(blockPos, color));
        }
        return list;
    }
}