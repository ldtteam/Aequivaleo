package com.ldtteam.aequivaleo.bootstrap;

import com.ldtteam.aequivaleo.api.recipe.IRecipeTypeProcessingRegistry;
import com.ldtteam.aequivaleo.api.util.Constants;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
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

    @SubscribeEvent
    public void onInterModProcess(final InterModProcessEvent event)
    {
        LOGGER.info("Processing IMC messages.");
        final List<InterModComms.IMCMessage> imcMessages = event.getIMCStream().collect(Collectors.toList());
        imcMessages
          .stream()
          .filter(imcMessage -> imcMessage.getMethod().equals("registerRecipeTypeCallback"))
          .forEach(imcMessage -> {
              final Supplier<Consumer<BiConsumer<ResourceLocation, IRecipeType<?>[]>>> message = imcMessage.getMessageSupplier();
              message.get().accept((location, iRecipeTypes) -> IRecipeTypeProcessingRegistry.getInstance()
                .registerAs(location, iRecipeTypes));
          });
    }
}
