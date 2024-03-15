package com.ldtteam.aequivaleo.network;

import com.google.common.eventbus.Subscribe;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.network.messages.CompoundTypeSyncedRegistryNetworkPayload;
import com.ldtteam.aequivaleo.network.messages.EquivalencyResultsPayload;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

/**
 * Our wrapper for Forge network layer
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NetworkChannel
{
    private static final String LATEST_PROTO_VER = "1.0";

    @Subscribe
    private static void onNetworkRegistration(final RegisterPayloadHandlerEvent payloadHandlerEvent) {
        final IPayloadRegistrar registrar = payloadHandlerEvent.registrar(Constants.MOD_ID).versioned(LATEST_PROTO_VER);

        registrar.configuration(
                CompoundTypeSyncedRegistryNetworkPayload.ID,
                CompoundTypeSyncedRegistryNetworkPayload::new,
                handler -> handler.client(CompoundTypeSyncedRegistryNetworkPayload::onExecute)
        );
        registrar.common(
                EquivalencyResultsPayload.ID,
                EquivalencyResultsPayload::new,
                handler -> handler.client(EquivalencyResultsPayload::onExecute)
        );
    }
}
