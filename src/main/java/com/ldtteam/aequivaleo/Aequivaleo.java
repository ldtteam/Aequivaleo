package com.ldtteam.aequivaleo;

import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.plugin.IAequivaleoPlugin;
import com.ldtteam.aequivaleo.api.tags.Tags;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.api.util.StreamUtils;
import com.ldtteam.aequivaleo.apiimpl.AequivaleoAPI;
import com.ldtteam.aequivaleo.bootstrap.CompoundContainerTypeRegistrar;
import com.ldtteam.aequivaleo.bootstrap.CompoundTypesRegistrar;
import com.ldtteam.aequivaleo.bootstrap.RecipeIngredientTypeRegistrar;
import com.ldtteam.aequivaleo.config.Configuration;
import com.ldtteam.aequivaleo.plugin.PluginManger;
import com.ldtteam.aequivaleo.recipe.equivalency.RecipeCalculatorLogHandler;
import com.ldtteam.aequivaleo.utils.AnalysisLogHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Constants.MOD_ID)
public class Aequivaleo
{

    private static Aequivaleo INSTANCE;
    private static final Logger LOGGER = LogManager.getLogger();

    private final Configuration configuration;

    public Aequivaleo(IEventBus modBus)
    {
        LOGGER.info("Aequivaleo is being instantiated.");

        RecipeCalculatorLogHandler.setupLogging();

        CompoundContainerTypeRegistrar.COMPOUND_CONTAINER_FACTORY_REGISTRY.register(modBus);
        CompoundTypesRegistrar.COMPOUND_TYPE_GROUP_REGISTRY.register(modBus);
        CompoundTypesRegistrar.COMPOUND_TYPE_REGISTRY.register(modBus);
        RecipeIngredientTypeRegistrar.RECIPE_INGREDIENT_TYPE_REGISTRY.register(modBus);

        INSTANCE = this;
        IAequivaleoAPI.Holder.setInstance(AequivaleoAPI.getInstance());
        StreamUtils.setup(IAequivaleoAPI.getInstance());

        configuration = new Configuration(ModLoadingContext.get().getActiveContainer());

        PluginManger.getInstance().detect();
        PluginManger.getInstance().run(IAequivaleoPlugin::onConstruction);

        modBus.addListener(AnalysisLogHandler::onConfigurationReloaded);

        Tags.init();
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
