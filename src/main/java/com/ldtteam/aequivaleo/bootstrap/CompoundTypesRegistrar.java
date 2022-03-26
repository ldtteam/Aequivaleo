package com.ldtteam.aequivaleo.bootstrap;

import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.ldtteam.aequivaleo.registry.ForgeRegistryBackedSyncedRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CompoundTypesRegistrar
{

    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onRegisterNewRegistry(final NewRegistryEvent event)
    {
        LOGGER.info("Registering the compound type group registry with forge.");
        ModRegistries.COMPOUND_TYPE_GROUP = event.create(makeCompoundTypeGroupsRegistry());

        LOGGER.info("Registering the compound type registry with forge.");
        ModRegistries.COMPOUND_TYPE = new ForgeRegistryBackedSyncedRegistry<>(
          event.create(makeCompoundTypesRegistry()),
          ModRegistries.COMPOUND_TYPE_GROUP
        );
    }

    private static RegistryBuilder<ICompoundType> makeCompoundTypesRegistry(){
        return new RegistryBuilder<ICompoundType>()
                 .setName(new ResourceLocation(Constants.MOD_ID, "compound_type"))
                 .setIDRange(1, Integer.MAX_VALUE - 1)
                 .disableSaving()
                 .setType(ICompoundType.class);
    }

    private static RegistryBuilder<ICompoundTypeGroup> makeCompoundTypeGroupsRegistry() {
        return new RegistryBuilder<ICompoundTypeGroup>()
          .setName(new ResourceLocation(Constants.MOD_ID, "compound_type_group"))
          .setIDRange(1, Integer.MAX_VALUE - 1)
          .disableSaving()
          .setType(ICompoundTypeGroup.class);
    }
}
