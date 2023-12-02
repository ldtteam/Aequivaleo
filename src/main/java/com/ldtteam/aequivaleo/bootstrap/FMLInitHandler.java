package com.ldtteam.aequivaleo.bootstrap;

import com.ldtteam.aequivaleo.api.recipe.IRecipeTypeProcessingRegistry;
import com.ldtteam.aequivaleo.api.util.Constants;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class FMLInitHandler
{
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("Setting up aequivaleo static data.");
        CommonBootstrapper.doBootstrap();
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public static void onInterModProcess(final InterModProcessEvent event)
    {
        LOGGER.info("Processing IMC messages.");
        final List<InterModComms.IMCMessage> imcMessages = event.getIMCStream().toList();
        imcMessages
          .stream()
          .filter(imcMessage -> imcMessage.method().equals("registerRecipeTypeCallback"))
          .forEach(imcMessage -> {
              final Supplier<Consumer<BiConsumer<ResourceLocation, RecipeType<?>[]>>> message = (Supplier<Consumer<BiConsumer<ResourceLocation, RecipeType<?>[]>>>) imcMessage.messageSupplier();
              message.get().accept((location, iRecipeTypes) -> IRecipeTypeProcessingRegistry.getInstance()
                .registerAs(location, iRecipeTypes));
          });
    }
}
