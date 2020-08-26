package com.ldtteam.aequivaleo.network.messages;

import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.results.ResultsInformationCache;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SyncResultsMessage implements IMessage
{
    ResourceLocation                                  worldKey = new ResourceLocation("missingno");
    Map<ICompoundContainer<?>, Set<CompoundInstance>> data     = new HashMap<>();

    public SyncResultsMessage(
      @NotNull final PacketBuffer buffer
    ) {
        this.fromBytes(buffer);
    }

    public SyncResultsMessage(
      final RegistryKey<World> worldKey,
      final Map<ICompoundContainer<?>, Set<CompoundInstance>> data) {
        this.worldKey = worldKey.func_240901_a_();
        this.data = data;
    }

    @Override
    public void toBytes(final PacketBuffer buf)
    {
        buf.writeResourceLocation(worldKey);
        buf.writeInt(data.size());
        data.forEach((iCompoundContainer, iCompoundInstances) -> {
            
        });
    }

    private void fromBytes(final PacketBuffer buffer) {

    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final RegistryKey<World> worldRegistryKey = RegistryKey.func_240903_a_(Registry.WORLD_KEY, worldKey);
        ResultsInformationCache.getInstance(worldRegistryKey).set(data);
    }
}
