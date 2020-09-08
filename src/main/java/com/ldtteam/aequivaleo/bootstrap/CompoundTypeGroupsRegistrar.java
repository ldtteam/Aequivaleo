package com.ldtteam.aequivaleo.bootstrap;

import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CompoundTypeGroupsRegistrar
{

    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onRegisterNewRegistry(final RegistryEvent.NewRegistry event)
    {
        LOGGER.info("Registering the compound type group registry with forge.");
        makeRegistry("compound_type_group", ICompoundTypeGroup.class).create();
        ModRegistries.COMPOUND_TYPE = RegistryManager.ACTIVE.getRegistry(ICompoundType.class);
    }

    private static <T extends IForgeRegistryEntry<T>> RegistryBuilder<T> makeRegistry(String name, Class<T> type) {
        return new RegistryBuilder<T>()
                 .setName(new ResourceLocation(Constants.MOD_ID, name))
                 .setIDRange(1, Integer.MAX_VALUE - 1)
                 .disableSaving()
                 .setType(type);
    }
}
