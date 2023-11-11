package com.ldtteam.aequivaleo.vanilla;

import com.ldtteam.aequivaleo.api.plugin.AequivaleoPlugin;
import com.ldtteam.aequivaleo.api.plugin.IAequivaleoPlugin;
import com.ldtteam.aequivaleo.vanilla.data.AequivaleoInformationProvider;
import net.minecraftforge.data.event.GatherDataEvent;

@AequivaleoPlugin
public class ValidationAequivaleoPlugin implements IAequivaleoPlugin {
    
    @Override
    public String getId() {
        return "validation";
    }
    
    @Override
    public void onConstruction() {
        Registry.init();
    }
    
    @Override
    public void onGatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(event.includeServer(), new AequivaleoInformationProvider(event.getGenerator()));
    }
}
