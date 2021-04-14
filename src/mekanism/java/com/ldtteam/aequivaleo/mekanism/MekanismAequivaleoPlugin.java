package com.ldtteam.aequivaleo.mekanism;

import com.ldtteam.aequivaleo.api.plugin.AequivaleoPlugin;
import com.ldtteam.aequivaleo.api.plugin.IAequivaleoPlugin;
import com.ldtteam.aequivaleo.api.recipe.IRecipeTypeProcessingRegistry;
import com.ldtteam.aequivaleo.mekanism.api.util.Constants;
import mekanism.api.MekanismAPI;
import mekanism.common.recipe.MekanismRecipeType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@AequivaleoPlugin(requiredMods = {"mekanism"})
public class MekanismAequivaleoPlugin implements IAequivaleoPlugin
{
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public String getId()
    {
        return "Mekanism";
    }

    @Override
    public void onConstruction()
    {
        LOGGER.info("Started Aequivaleo mekanism plugin.");
        LOGGER.debug("  > Running against: " + MekanismAPI.API_VERSION);
    }

    @Override
    public void onCommonSetup()
    {
        LOGGER.info("Running aequivaleo mekanism plugin common setup.");

        LOGGER.debug("Registering recipe processing types.");
        IRecipeTypeProcessingRegistry.getInstance()
            .registerAs(Constants.SIMPLE_ITEMSTACK_TO_ITEMSTACK, MekanismRecipeType.CRUSHING, MekanismRecipeType.ENRICHING, MekanismRecipeType.SMELTING)
            .registerAs(Constants.CHEMICAL_INFUSING, MekanismRecipeType.CHEMICAL_INFUSING)
            .registerAs(Constants.COMBINING, MekanismRecipeType.COMBINING)
            .registerAs(Constants.SEPARATING, MekanismRecipeType.SEPARATING)
            .registerAs(Constants.WASHING, MekanismRecipeType.WASHING)
            .registerAs(Constants.EVAPORATING, MekanismRecipeType.EVAPORATING)
            .registerAs(Constants.SIMPLE_GAS_TO_GAS, MekanismRecipeType.ACTIVATING, MekanismRecipeType.CENTRIFUGING)
            .registerAs(Constants.CRYSTALLIZING, MekanismRecipeType.CRYSTALLIZING)
            .registerAs(Constants.DISSOLUTION, MekanismRecipeType.DISSOLUTION)
            .registerAs(Constants.SIMPLE_ITEMSTACK_GAS_TO_ITEMSTACK, MekanismRecipeType.COMPRESSING, MekanismRecipeType.PURIFYING, MekanismRecipeType.INJECTING)
            .registerAs(Constants.NUCLEOSYNTHESIZING, MekanismRecipeType.NUCLEOSYNTHESIZING)
            .registerAs(Constants.ENERGY_CONVERSION, MekanismRecipeType.ENERGY_CONVERSION)
            .registerAs(Constants.SIMPLE_ITEMSTACK_TO_GAS, MekanismRecipeType.GAS_CONVERSION, MekanismRecipeType.OXIDIZING)
            .registerAs(Constants.INFUSION_CONVERSION, MekanismRecipeType.INFUSION_CONVERSION)
            .registerAs(Constants.METALLURGIC_INFUSING, MekanismRecipeType.METALLURGIC_INFUSING)
            .registerAs(Constants.REACTION, MekanismRecipeType.REACTION)
            .registerAs(Constants.ROTARY, MekanismRecipeType.ROTARY)
            .registerAs(Constants.SAWING, MekanismRecipeType.SAWING);
    }
}
