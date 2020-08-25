package com.ldtteam.aequivaleo.bootstrap;

import com.ldtteam.aequivaleo.api.compound.ICompoundType;
import com.ldtteam.aequivaleo.api.util.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CompoundTypesRegistrar
{

    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onRegisterNewRegistry(final RegistryEvent.NewRegistry event)
    {
        LOGGER.info("Registering the compound type registry with forge.");
        new RegistryBuilder<ICompoundType>()
          .setType(ICompoundType.class)
          .setName(new ResourceLocation(Constants.MOD_ID, "compound_type"))
          .allowModification()
          .create();
    }
}
