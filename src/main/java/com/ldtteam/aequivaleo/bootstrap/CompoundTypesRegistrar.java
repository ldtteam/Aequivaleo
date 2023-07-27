package com.ldtteam.aequivaleo.bootstrap;

import com.google.common.base.Suppliers;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.ldtteam.aequivaleo.api.util.RegistryUtils;
import com.ldtteam.aequivaleo.registry.ForgeRegistryBackedSyncedRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CompoundTypesRegistrar
{
    private static final ResourceLocation TYPE_REGISTRY_NAME = new ResourceLocation(Constants.MOD_ID, "compound_type");
    private static final ResourceLocation GROUP_REGISTRY_NAME = new ResourceLocation(Constants.MOD_ID, "compound_type_group");
    private static final Logger LOGGER = LogManager.getLogger();

    public static final DeferredRegister<ICompoundType> COMPOUND_TYPE_REGISTRY = DeferredRegister.create(
            ResourceKey.createRegistryKey(TYPE_REGISTRY_NAME),
            Constants.MOD_ID
    );

    public static final DeferredRegister<ICompoundTypeGroup> COMPOUND_TYPE_GROUP_REGISTRY = DeferredRegister.create(
            ResourceKey.createRegistryKey(GROUP_REGISTRY_NAME),
            Constants.MOD_ID
    );

    static {
        ModRegistries.COMPOUND_TYPE_GROUP = COMPOUND_TYPE_GROUP_REGISTRY.makeRegistry(() -> RegistryUtils.makeRegistry(ICompoundTypeGroup.class));
        final Supplier<IForgeRegistry<ICompoundType>> backingRegistry = COMPOUND_TYPE_REGISTRY.makeRegistry(() -> RegistryUtils.makeSyncedRegistry(ICompoundType.class, ModRegistries.COMPOUND_TYPE_GROUP));
        ModRegistries.COMPOUND_TYPE = Suppliers.memoize(() -> new ForgeRegistryBackedSyncedRegistry<>(
                ResourceKey.createRegistryKey(TYPE_REGISTRY_NAME),
                backingRegistry,
                ModRegistries.COMPOUND_TYPE_GROUP
        ));
    }
}
