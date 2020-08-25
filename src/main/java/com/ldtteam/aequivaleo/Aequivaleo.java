package com.ldtteam.aequivaleo;

import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.config.Configuration;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Constants.MOD_ID)
public class Aequivaleo
{

    private static Aequivaleo INSTANCE;
    private static final Logger LOGGER = LogManager.getLogger();

    private final Configuration configuration;
    
    public Aequivaleo()
    {
        INSTANCE = this;

        LOGGER.info("Aequivaleo is being instantiated.");
        configuration = new Configuration(ModLoadingContext.get().getActiveContainer());
    }

    public static Aequivaleo getInstance()
    {
        return INSTANCE;
    }

    public Configuration getConfiguration()
    {
        return configuration;
    }
}
