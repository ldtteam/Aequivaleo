package com.ldtteam.aequivaleo.bootstrap;

import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.ldtteam.aequivaleo.api.util.RegistryUtils;
import com.ldtteam.aequivaleo.compound.container.fluid.FluidContainer;
import com.ldtteam.aequivaleo.compound.container.fluid.FluidStackContainer;
import com.ldtteam.aequivaleo.compound.container.heat.HeatContainer;
import com.ldtteam.aequivaleo.compound.container.itemstack.ItemContainer;
import com.ldtteam.aequivaleo.compound.container.itemstack.ItemStackContainer;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CompoundContainerFactoryRegistrar
{
    private static final Logger LOGGER = LogManager.getLogger();

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public static void onRegisterRegistry(@NotNull RegistryEvent.NewRegistry event) {
        LOGGER.info("Registering the container factory registry with forge.");
        RegistryUtils.makeRegistry("container_factory", ICompoundContainerFactory.class)
          .onBake((owner, stage) -> {
              LOGGER.info("Received bake callback for the container factory registry. Triggering baking of type map on manager.");
              CompoundContainerFactoryManager.getInstance().bake();
          }).create();
        ModRegistries.CONTAINER_FACTORY = RegistryManager.ACTIVE.getRegistry(ICompoundContainerFactory.class);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDataGeneration(GatherDataEvent event) {
        LOGGER.info("Data generation triggered. Baking container factory manager.");
        CompoundContainerFactoryManager.getInstance().bake();
    }

    @SubscribeEvent
    public static void onRegisterFactories(@NotNull RegistryEvent.Register<ICompoundContainerFactory<?>> event) {
        final IForgeRegistry<ICompoundContainerFactory<?>> registry = event.getRegistry();

        ModContainerFactoryTypes.ITEM = new ItemContainer.Factory();
        ModContainerFactoryTypes.ITEMSTACK = new ItemStackContainer.Factory();
        ModContainerFactoryTypes.FLUID = new FluidContainer.Factory();
        ModContainerFactoryTypes.FLUIDSTACK = new FluidStackContainer.Factory();
        ModContainerFactoryTypes.HEAT = new HeatContainer.Factory();

        registry.registerAll(
          ModContainerFactoryTypes.ITEM,
          ModContainerFactoryTypes.ITEMSTACK,
          ModContainerFactoryTypes.FLUID,
          ModContainerFactoryTypes.FLUIDSTACK,
          ModContainerFactoryTypes.HEAT
        );
    }
}
