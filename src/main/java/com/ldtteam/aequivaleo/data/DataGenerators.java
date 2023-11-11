package com.ldtteam.aequivaleo.data;

import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.util.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        if (event.includeServer()) {
            final BlockTagProvider blockTagProvider = new BlockTagProvider(
                    event.getGenerator().getPackOutput(),
                    event.getLookupProvider(),
                    event.getExistingFileHelper()
            );
            
            event.getGenerator().addProvider(
                    event.includeServer(),
                    blockTagProvider
            );
            
            event.getGenerator().addProvider(
                    event.includeServer(),
                    new ItemTagProvider(
                            event.getGenerator().getPackOutput(),
                            event.getLookupProvider(),
                            blockTagProvider.contentsGetter(),
                            event.getExistingFileHelper()
                    )
            );
        }
        
        IAequivaleoAPI.getInstance().getPluginManager().getPlugins()
                .forEach(plugin -> plugin.onGatherData(event));
    }
}