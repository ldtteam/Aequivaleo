package com.ldtteam.aequivaleo;

import com.ldtteam.aequivaleo.api.AequivaleoAPI;
import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.plugin.IAequivaleoPlugin;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.config.Configuration;
import com.ldtteam.aequivaleo.network.NetworkChannel;
import com.ldtteam.aequivaleo.plugin.PluginManger;
import com.ldtteam.aequivaleo.recipe.equivalency.RecipeCalculatorLogHandler;
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
    private final NetworkChannel networkChannel;

    public Aequivaleo()
    {
        LOGGER.info("Aequivaleo is being instantiated.");

        RecipeCalculatorLogHandler.setupLogging();

        INSTANCE = this;
        IAequivaleoAPI.Holder.setInstance(AequivaleoAPI.getInstance());
        configuration = new Configuration(ModLoadingContext.get().getActiveContainer());
        networkChannel = new NetworkChannel(Constants.MOD_ID);

        PluginManger.getInstance().detect();
        PluginManger.getInstance().run(IAequivaleoPlugin::onConstruction);
    }

    public static Aequivaleo getInstance()
    {
        return INSTANCE;
    }

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public NetworkChannel getNetworkChannel()
    {
        return networkChannel;
    }
}
