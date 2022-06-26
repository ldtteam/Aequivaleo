package com.ldtteam.aequivaleo.bootstrap;

import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.ldtteam.aequivaleo.api.util.RegistryUtils;
import com.ldtteam.aequivaleo.compound.container.compoundtype.CompoundTypeContainer;
import com.ldtteam.aequivaleo.compound.container.fluid.FluidContainer;
import com.ldtteam.aequivaleo.compound.container.fluid.FluidStackContainer;
import com.ldtteam.aequivaleo.compound.container.heat.HeatContainer;
import com.ldtteam.aequivaleo.compound.container.itemstack.ItemContainer;
import com.ldtteam.aequivaleo.compound.container.itemstack.ItemStackContainer;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.compound.container.tag.TagContainer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unchecked")
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CompoundContainerFactoryRegistrar
{
    private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Constants.MOD_ID, "container_factory");
    private static final Logger LOGGER = LogManager.getLogger();

    public static final DeferredRegister<ICompoundContainerFactory<?>> COMPOUND_CONTAINER_FACTORY_REGISTRY = DeferredRegister.create(
            ResourceKey.createRegistryKey(REGISTRY_NAME),
            Constants.MOD_ID
    );

    static {
        ModRegistries.CONTAINER_FACTORY = COMPOUND_CONTAINER_FACTORY_REGISTRY.makeRegistry(() -> ((RegistryBuilder<ICompoundContainerFactory<?>>) (Object)  RegistryUtils.makeRegistry(ICompoundContainerFactory.class)
                .onBake((owner, stage) -> CompoundContainerFactoryManager.getInstance().bake())
        ));

        ModContainerFactoryTypes.ITEM = COMPOUND_CONTAINER_FACTORY_REGISTRY.register("item", ItemContainer.Factory::new);
        ModContainerFactoryTypes.ITEMSTACK = COMPOUND_CONTAINER_FACTORY_REGISTRY.register("itemstack", ItemStackContainer.Factory::new);
        ModContainerFactoryTypes.FLUID = COMPOUND_CONTAINER_FACTORY_REGISTRY.register("fluid", FluidContainer.Factory::new);
        ModContainerFactoryTypes.FLUIDSTACK = COMPOUND_CONTAINER_FACTORY_REGISTRY.register("fluidstack", FluidStackContainer.Factory::new);
        ModContainerFactoryTypes.HEAT = COMPOUND_CONTAINER_FACTORY_REGISTRY.register("heat", HeatContainer.Factory::new);
        ModContainerFactoryTypes.COMPOUND_TYPE = COMPOUND_CONTAINER_FACTORY_REGISTRY.register("compound_type", CompoundTypeContainer.Factory::new);
        ModContainerFactoryTypes.TAG = COMPOUND_CONTAINER_FACTORY_REGISTRY.register("tag", TagContainer.Factory::new);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDataGeneration(GatherDataEvent event) {
        LOGGER.info("Data generation triggered. Baking container factory manager.");
        CompoundContainerFactoryManager.getInstance().bake();
    }
}
