package com.ldtteam.aequivaleo;

import com.ldtteam.aequivaleo.api.util.Constants;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Constants.MOD_ID)
public class Aequivaleo
{

    private static final Logger LOGGER = LogManager.getLogger();

    public Aequivaleo()
    {
        LOGGER.info("Aequivaleo is being instantiated.");
    }
}
