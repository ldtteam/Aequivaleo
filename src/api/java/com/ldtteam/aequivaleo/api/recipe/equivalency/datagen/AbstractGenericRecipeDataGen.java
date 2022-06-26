package com.ldtteam.aequivaleo.api.recipe.equivalency.datagen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IGenericRecipeEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.data.GenericRecipeData;
import com.ldtteam.aequivaleo.api.recipe.equivalency.data.GenericRecipeDataBuilder;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.util.Constants;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("SameParameterValue")
public abstract class AbstractGenericRecipeDataGen implements DataProvider
{

    private final DataGenerator dataGenerator;
    @VisibleForTesting
    final         WorldData     generalData  = new WorldData(new ResourceLocation(Constants.MOD_ID, "general")) {
        @Override
        public String getPath()
        {
            return "general";
        }
    };
    @VisibleForTesting
    final Map<ResourceLocation, WorldData> worldDataMap = Maps.newHashMap();

    protected AbstractGenericRecipeDataGen(final DataGenerator dataGenerator) {this.dataGenerator = dataGenerator;}

    @Override
    public void run(@NotNull final CachedOutput cache) throws IOException
    {
        this.calculateDataToSave();

        final Gson gson = IAequivaleoAPI.getInstance().getGson(ICondition.IContext.EMPTY);

        this.writeData(
          cache,
          gson,
          generalData
        );

        for (WorldData worldData : worldDataMap.values())
        {
            this.writeData(
              cache,
              gson,
              worldData
            );
        }
    }

    @VisibleForTesting
    void writeData(
      final CachedOutput cache,
      final Gson gson,
      final WorldData worldData
    ) throws IOException
    {
        for (Map.Entry<ResourceLocation, GenericRecipeData> entry : worldData.getRecipes().entrySet())
        {
            ResourceLocation name = entry.getKey();

            final Path itemPath = dataGenerator.getOutputFolder().resolve(String.format("data/%s/aequivaleo/recipes/%s", name.getNamespace(), worldData.getPath())).resolve(String.format("%s.json", name.getPath()));

            DataProvider.saveStable(
              cache,
              gson.toJsonTree(entry.getValue()),
              itemPath
            );
        }
    }

    public abstract void calculateDataToSave();

    protected void saveData(
      final ResourceLocation worldId,
      final ResourceLocation name,
      final Set<IRecipeIngredient> inputs,
      final Set<ICompoundContainer<?>> outputs) {
        this.saveData(
          worldId,
          name,
          inputs,
          Collections.emptySet(),
          outputs
        );
    }

    protected void saveData(
      final ResourceLocation worldId,
      final IGenericRecipeEquivalencyRecipe recipeEquivalencyRecipe
    ) {
        this.saveData(
          worldId,
          recipeEquivalencyRecipe.getRecipeName(),
          recipeEquivalencyRecipe.getInputs(),
          recipeEquivalencyRecipe.getRequiredKnownOutputs(),
          recipeEquivalencyRecipe.getOutputs()
        );
    }

    protected void saveData(
      final ResourceLocation worldId,
      final ResourceLocation name,
      final Set<IRecipeIngredient> inputs,
      final Set<ICompoundContainer<?>> requiredKnownOutputs,
      final Set<ICompoundContainer<?>> outputs) {
        this.saveData(
          worldId,
          name,
          inputs,
          requiredKnownOutputs,
          outputs,
          Sets.newHashSet()
        );
    }

    protected void saveData(
      final ResourceLocation worldId,
      final ResourceLocation name,
      final Set<IRecipeIngredient> inputs,
      final Set<ICompoundContainer<?>> requiredKnownOutputs,
      final Set<ICompoundContainer<?>> outputs,
      final Set<ICondition> conditions) {
        this.saveData(
          worldId,
          name,
          new GenericRecipeDataBuilder()
            .setInputs(inputs)
            .setRequiredKnownOutputs(requiredKnownOutputs)
            .setOutputs(outputs)
            .setConditions(conditions)
        );
    }

    protected void saveData(
      final ResourceLocation worldId,
      final ResourceLocation name,
      final GenericRecipeDataBuilder builder) {
        this.worldDataMap.computeIfAbsent(worldId, WorldData::new).recipes.put(name,
          builder
            .createGenericRecipeData());
    }

    protected void saveData(
      final ResourceLocation name,
      final Set<IRecipeIngredient> inputs,
      final Set<ICompoundContainer<?>> outputs) {
        this.saveData(
          name,
          inputs,
          Collections.emptySet(),
          outputs
        );
    }

    protected void saveData(
      final IGenericRecipeEquivalencyRecipe recipeEquivalencyRecipe
    ) {
        this.saveData(
          recipeEquivalencyRecipe.getRecipeName(),
          recipeEquivalencyRecipe.getInputs(),
          recipeEquivalencyRecipe.getRequiredKnownOutputs(),
          recipeEquivalencyRecipe.getOutputs()
        );
    }

    protected void saveData(
      final ResourceLocation name,
      final Set<IRecipeIngredient> inputs,
      final Set<ICompoundContainer<?>> requiredKnownOutputs,
      final Set<ICompoundContainer<?>> outputs) {
        this.saveData(
          name,
          inputs,
          requiredKnownOutputs,
          outputs,
          Sets.newHashSet()
        );
    }

    protected void saveData(
      final ResourceLocation name,
      final Set<IRecipeIngredient> inputs,
      final Set<ICompoundContainer<?>> requiredKnownOutputs,
      final Set<ICompoundContainer<?>> outputs,
      final Set<ICondition> conditions) {
        this.saveData(
          name,
          new GenericRecipeDataBuilder()
            .setInputs(inputs)
            .setRequiredKnownOutputs(requiredKnownOutputs)
            .setOutputs(outputs)
            .setConditions(conditions)
        );
    }

    protected void saveData(
      final ResourceLocation name,
      final GenericRecipeDataBuilder builder
    ) {
        generalData.recipes.put(name,
          builder
            .createGenericRecipeData());
    }

    private static class WorldData {
        private final ResourceLocation                         worldId;
        private final Map<ResourceLocation, GenericRecipeData> recipes = Maps.newHashMap();

        private WorldData(final ResourceLocation worldId) {this.worldId = worldId;}

        public ResourceLocation getWorldId()
        {
            return worldId;
        }

        public Map<ResourceLocation, GenericRecipeData> getRecipes()
        {
            return recipes;
        }

        public String getPath() {
            return worldId.getNamespace() + "/" + worldId.getPath();
        }
    }

}
