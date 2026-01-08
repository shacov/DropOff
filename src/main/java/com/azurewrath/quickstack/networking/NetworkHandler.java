package com.azurewrath.quickstack.networking;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import com.azurewrath.quickstack.QuickStack;

public class NetworkHandler {
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(QuickStack.MOD_ID)
                .versioned("1.0");

        registrar.playToServer(C2SFavoriteItemPayload.TYPE, C2SFavoriteItemPayload.STREAM_CODEC, C2SFavoriteItemPayload::handle);
        registrar.playToServer(C2SRequestQuickstackPayload.TYPE, C2SRequestQuickstackPayload.STREAM_CODEC, C2SRequestQuickstackPayload::handle);
        registrar.playToClient(S2CReportPayload.TYPE, S2CReportPayload.STREAM_CODEC, S2CReportPayload::handle);
    }
}
