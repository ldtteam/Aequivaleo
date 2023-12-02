package com.ldtteam.aequivaleo.bootstrap;

import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.ldtteam.aequivaleo.registry.ForgeRegistryBackedSyncedRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        ModRegistries.COMPOUND_TYPE_GROUP = COMPOUND_TYPE_GROUP_REGISTRY.makeRegistry(builder -> builder.maxId(Integer.MAX_VALUE));
        final Registry<ICompoundType> backingRegistry = COMPOUND_TYPE_REGISTRY.makeRegistry(builder -> builder.maxId(Integer.MAX_VALUE));
        ModRegistries.COMPOUND_TYPE = new ForgeRegistryBackedSyncedRegistry<>(
                ResourceKey.createRegistryKey(TYPE_REGISTRY_NAME),
                backingRegistry,
                ModRegistries.COMPOUND_TYPE_GROUP
        );
    }
}
