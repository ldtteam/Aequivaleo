package com.ldtteam.aequivaleo.bootstrap;

import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerType;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.ldtteam.aequivaleo.compound.container.compoundtype.CompoundTypeContainer;
import com.ldtteam.aequivaleo.compound.container.fluid.FluidContainer;
import com.ldtteam.aequivaleo.compound.container.fluid.FluidStackContainer;
import com.ldtteam.aequivaleo.compound.container.heat.HeatContainer;
import com.ldtteam.aequivaleo.compound.container.itemstack.ItemContainer;
import com.ldtteam.aequivaleo.compound.container.itemstack.ItemStackContainer;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.compound.container.tag.TagContainer;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CompoundContainerTypeRegistrar
{
    private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Constants.MOD_ID, "container_factory");
    private static final Logger LOGGER = LogManager.getLogger();

    public static final DeferredRegister<ICompoundContainerType<?>> COMPOUND_CONTAINER_FACTORY_REGISTRY = DeferredRegister.create(
            ResourceKey.createRegistryKey(REGISTRY_NAME),
            Constants.MOD_ID
    );

    static {
        ModRegistries.CONTAINER_FACTORY = COMPOUND_CONTAINER_FACTORY_REGISTRY.makeRegistry((builder) -> {
            builder.maxId(Integer.MAX_VALUE)
                    .onBake((registry) -> CompoundContainerFactoryManager.getInstance().bake());
        });

        ModContainerTypes.ITEM = COMPOUND_CONTAINER_FACTORY_REGISTRY.register("item", ItemContainer.Type::new);
        ModContainerTypes.ITEMSTACK = COMPOUND_CONTAINER_FACTORY_REGISTRY.register("itemstack", ItemStackContainer.Type::new);
        ModContainerTypes.FLUID = COMPOUND_CONTAINER_FACTORY_REGISTRY.register("fluid", FluidContainer.Type::new);
        ModContainerTypes.FLUIDSTACK = COMPOUND_CONTAINER_FACTORY_REGISTRY.register("fluidstack", FluidStackContainer.Type::new);
        ModContainerTypes.HEAT = COMPOUND_CONTAINER_FACTORY_REGISTRY.register("heat", HeatContainer.Type::new);
        ModContainerTypes.COMPOUND_TYPE = COMPOUND_CONTAINER_FACTORY_REGISTRY.register("compound_type", CompoundTypeContainer.Type::new);
        ModContainerTypes.TAG = COMPOUND_CONTAINER_FACTORY_REGISTRY.register("tag", TagContainer.Type::new);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDataGeneration(GatherDataEvent event) {
        LOGGER.info("Data generation triggered. Baking container factory manager.");
        CompoundContainerFactoryManager.getInstance().bake();
    }
}
