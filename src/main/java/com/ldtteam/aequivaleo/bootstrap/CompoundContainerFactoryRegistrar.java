package com.ldtteam.aequivaleo.bootstrap;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.ldtteam.aequivaleo.compound.container.blockstate.BlockContainer;
import com.ldtteam.aequivaleo.compound.container.blockstate.BlockStateContainer;
import com.ldtteam.aequivaleo.compound.container.heat.HeatContainer;
import com.ldtteam.aequivaleo.compound.container.itemstack.ItemContainer;
import com.ldtteam.aequivaleo.compound.container.itemstack.ItemStackContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CompoundContainerFactoryRegistrar
{
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onRegisterRegistry(@NotNull RegistryEvent.NewRegistry event) {
        LOGGER.info("Registering the container factory registry with forge.");
        makeRegistry("container_factory", ICompoundContainerFactory.class).create();
        ModRegistries.CONTAINER_FACTORY = RegistryManager.ACTIVE.getRegistry(ICompoundContainerFactory.class);
    }

    private static <T extends IForgeRegistryEntry<T>> RegistryBuilder<T> makeRegistry(String name, Class<T> type) {
        return new RegistryBuilder<T>()
                 .setName(new ResourceLocation(Constants.MOD_ID, name))
                 .setIDRange(1, Integer.MAX_VALUE - 1)
                 .disableSaving()
                 .setType(type);
    }

    @SubscribeEvent
    public static void onRegisterFactories(@NotNull RegistryEvent.Register<ICompoundContainerFactory<?>> event) {
        final IForgeRegistry<ICompoundContainerFactory<?>> registry = event.getRegistry();

        ModContainerFactoryTypes.ITEM = new ItemContainer.Factory();
        ModContainerFactoryTypes.ITEMSTACK = new ItemStackContainer.Factory();
        ModContainerFactoryTypes.HEAT = new HeatContainer.Factory();
        ModContainerFactoryTypes.BLOCK = new BlockContainer.Factory();
        ModContainerFactoryTypes.BLOCKSTATE = new BlockStateContainer.Factory();

        registry.registerAll(
          ModContainerFactoryTypes.ITEM,
          ModContainerFactoryTypes.ITEMSTACK,
          ModContainerFactoryTypes.HEAT,
          ModContainerFactoryTypes.BLOCK,
          ModContainerFactoryTypes.BLOCKSTATE
        );
    }
}
